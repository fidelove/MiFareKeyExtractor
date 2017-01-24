package com.fidel.reader;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import org.usb4java.BufferUtils;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;

import com.fidel.exception.ReaderException;
import com.fidel.tools.ReaderTools;

public class ElatecReaderForMiFareClassic {

	private static final short VENDOR_ID = (short) Integer.parseInt("09D8", 16);
	private static final short PRODUCT_ID = (short) Integer.parseInt("0420", 16);
	private static final byte INTERFACE_USB = 1;
	private static final int TIMEOUT = 500;
	private static final byte IN_ENDPOINT = (byte) 0x82;
	private static final byte OUT_ENDPOINT = (byte) 0x02;
	private static final int MAX_SIZE_BUFFER = 64;
	private static final int MAX_COMM_TRY = 5;

	protected boolean bIsInitialized = false;
	protected Context usbContext;
	protected DeviceHandle usbHandle;

	private static ElatecReaderForMiFareClassic instance;

	public static ElatecReaderForMiFareClassic getInstance() {
		if (instance == null)
			instance = new ElatecReaderForMiFareClassic();
		return instance;
	}

	ElatecReaderForMiFareClassic() {
		super();
	}

	public void initChannel() throws ReaderException {
		if (bIsInitialized) {
			System.out.println(System.currentTimeMillis() + " : already initialized");
			return;
		}

		usbContext = new Context();
		int result = LibUsb.init(usbContext);
		if (result != LibUsb.SUCCESS) {
			throw new ReaderException("No context found");
		}

		getDeviceAndHandle();
		openInterface();
		setReaderConfiguration();

		bIsInitialized = true;

		setCommSettings();
		setResearchTag();
		clearBuffer();
	};

	public int huntCard() throws ReaderException {
		byte[] response = sendCmd(new byte[] { 5, 0, 16 });
		if (response != null && response[0] == 0x00 && response[1] == 0x01) {
			byte[] cardId = new byte[response[4]];
			System.arraycopy(response, 5, cardId, 0, cardId.length);
			return ByteBuffer.wrap(response).getInt();
		}
		return -1;
	}

	public boolean loginIntoBlockWithKey(int block, long key) throws ReaderException {

		boolean keyFound = false;
		byte[] byteKey = ReaderTools.longToByteArray(key);

		if (loginIntoBlockWithKey(block, byteKey, true)) {
			System.out.println("");
			System.out.println("the key " + ReaderTools.getByteArray(byteKey) + " is a valid A Key");
			keyFound = true;
		}

		if (loginIntoBlockWithKey(block, byteKey, false)) {
			System.out.println("the key " + ReaderTools.getByteArray(byteKey) + " is a valid A Key");
			keyFound = true;
		}
		return keyFound;
	}

	public boolean loginIntoBlockWithKey(int block, byte[] key, boolean isKeyA) throws ReaderException {

		ByteBuffer loginApduBuffer = ByteBuffer.allocateDirect(10);
		loginApduBuffer.put(new byte[] { 0X0B, 0x00 });
		loginApduBuffer.put(key, 0, key.length);
		loginApduBuffer.put(2 + key.length, (byte) (isKeyA ? 0x00 : 0x01));
		loginApduBuffer.put(3 + key.length, (byte) getBlockForSector(block));
		loginApduBuffer.position(0);

		byte[] loginApdu = new byte[10];
		loginApduBuffer.get(loginApdu, 0, 10);

		byte[] response = sendCmd(loginApdu);
		return (response != null && response.length == 2 && response[0] == 0 && response[1] == 1);
	}

	private void getDeviceAndHandle() throws ReaderException {
		DeviceList list = new DeviceList();
		try {

			int result = LibUsb.getDeviceList(usbContext, list);
			if (result < 0) {
				throw new ReaderException("Unable to get device list", result);
			}

			DeviceDescriptor descriptor = new DeviceDescriptor();
			for (Device device : list) {
				if ((result = LibUsb.getDeviceDescriptor(device, descriptor)) != LibUsb.SUCCESS) {
					throw new ReaderException("Unable to read device descriptor", result);
				}

				if (descriptor.idVendor() == VENDOR_ID && descriptor.idProduct() == PRODUCT_ID) {

					usbHandle = new DeviceHandle();
					result = LibUsb.open(device, usbHandle);
					if (result != LibUsb.SUCCESS) {
						LibUsb.exit(usbContext);
						throw new ReaderException("Error when open Device", result);
					}
					return;
				}
			}

		} finally {
			LibUsb.freeDeviceList(list, true);
		}

		throw new ReaderException("Unable to get the device " + VENDOR_ID + ":" + PRODUCT_ID);
	}

	private void openInterface() throws ReaderException {
		int result;
		if (LibUsb.kernelDriverActive(usbHandle, INTERFACE_USB) == 1) {
			if ((result = LibUsb.detachKernelDriver(usbHandle, INTERFACE_USB)) != LibUsb.SUCCESS) {
				throw new ReaderException("Can't detach interface from kernel", result);
			}
		}

		if ((result = LibUsb.claimInterface(usbHandle, INTERFACE_USB)) != LibUsb.SUCCESS) {
			throw new ReaderException("Error when claiming interface", result);
		}
	}

	private void setReaderConfiguration() throws ReaderException {
		int result = LibUsb.controlTransfer(usbHandle, (byte) 0x21, (byte) 0x22, (short) (0x01 | 0x02), (short) 0,
				BufferUtils.allocateByteBuffer(0), TIMEOUT);
		if (result != LibUsb.SUCCESS) {
			throw new ReaderException("Error when configure line", result);
		}
	}

	private void setCommSettings() throws ReaderException {
		byte[] response = sendCmd(new byte[] { 16, 3, 32, 1, 5, 116, 1, 1, 0, 24, 0, 0, 67, 0, -2, 0 });

		if (!(response != null && response.length >= 2 && response[0] == 0x00 && response[1] == 0x01)) {
			throw new ReaderException("Error when executing setCommSettings");
		}
	}

	private void setResearchTag() throws ReaderException {
		byte[] response = sendCmd(new byte[] { 5, 2, 0, 0, 0, 0, 3, 0, 0, 0 });

		if (!(response != null && response.length >= 1 && response[0] == 0x00)) {
			throw new ReaderException("Error when executing setCommSettings");
		}
	}

	private void clearBuffer() {
		LibUsb.bulkTransfer(usbHandle, IN_ENDPOINT, ByteBuffer.allocateDirect(MAX_SIZE_BUFFER),
				BufferUtils.allocateIntBuffer(), TIMEOUT);
	}

	private byte getBlockForSector(int block) throws ReaderException {

		if (block > 3 && block < 64) {
			return (byte) (block / 4);
		} else {
			throw new ReaderException("The Mifare Classic card contains only 64 sectors");
		}
	}

	private byte[] sendCmd(byte[] arrayCmd) throws ReaderException {
		if (!bIsInitialized) {
			throw new ReaderException("The channel has not been initialised");
		}

		// Buffer for the request
		ByteBuffer bufferCmd = ByteBuffer.allocateDirect(arrayCmd.length + 2);
		bufferCmd.put(new byte[] { (byte) arrayCmd.length, 0 });
		bufferCmd.put(arrayCmd);

		// TODO: delete
		// System.out.print(" send -> ");
		// ReaderTools.printByteArray(arrayCmd);

		// Buffer for the number of bytes actually transferred
		IntBuffer transfered = BufferUtils.allocateIntBuffer();
		int result = LibUsb.bulkTransfer(usbHandle, OUT_ENDPOINT, bufferCmd, transfered, TIMEOUT);

		if (result != LibUsb.SUCCESS || transfered.get(0) < bufferCmd.capacity()) {
			throw new ReaderException(System.currentTimeMillis() + " : Error when write bulk transfer :" + result);
		}

		// Buffer for the partial responses
		ByteBuffer bufferTemp = ByteBuffer.allocateDirect(MAX_SIZE_BUFFER);
		// Buffer for the complete final response
		ByteBuffer bufferResp = ByteBuffer.allocateDirect(MAX_SIZE_BUFFER);
		transfered = BufferUtils.allocateIntBuffer();
		int retries = 0;
		int sizeResp = 0;

		// loop for get the complete response
		do {
			result = LibUsb.bulkTransfer(usbHandle, IN_ENDPOINT, bufferTemp, transfered, TIMEOUT);
			sizeResp += transfered.get(0);

			if (result == LibUsb.SUCCESS) {
				bufferResp.put(bufferTemp);
			}
		} while (retries++ < MAX_COMM_TRY && (result != LibUsb.SUCCESS || sizeResp != (bufferResp.get(0) + 2)));

		if (sizeResp < (bufferResp.get(0) - 2)) {
			throw new ReaderException(System.currentTimeMillis() + " : Error when write bulk transfer :" + result);
		}

		// Insert the response into a byte array
		bufferResp.clear();
		byte[] arrayRawRep = new byte[sizeResp];
		bufferResp.get(arrayRawRep, 0, sizeResp);

		byte[] response = Arrays.copyOfRange(arrayRawRep, 2, sizeResp);

		// TODO: delete
		// System.out.print("receive <- ");
		// ReaderTools.printByteArray(response);

		return response;
	}
}