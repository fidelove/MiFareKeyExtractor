package com.fidel.tools;

import java.nio.charset.StandardCharsets;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;

public class ReaderTools {

	/**
	 * Prints a byte array with hexadecimal format
	 * 
	 * @param byteArray
	 */
	public static void printByteArray(byte[] byteArray) {
		if (byteArray != null && byteArray.length > 0) {
			System.out.print(ReaderTools.getByteArray(byteArray));
		}
	}

	/**
	 * 
	 * @param byteArray
	 */
	public static String getByteArray(byte[] byteArray) {

		String stringByteArray = null;
		if (byteArray != null && byteArray.length > 0) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < byteArray.length; j++) {
				sb.append("0x").append(String.format("%02X ", byteArray[j])).append(" ");
			}

			stringByteArray = sb.append("\n").toString();
		}
		return stringByteArray;
	}

	/**
	 * Transform a hexadecimal coded byte array into a string
	 * 
	 * @param bytes
	 *            hexadecimal coded byte array
	 * @return the string
	 */
	public static String byteArrayToString(byte[] bytes) {
		return DatatypeConverter.printHexBinary(bytes);
	}

	/**
	 * Transform an ascii string into a hexadecimal coded byte array
	 * 
	 * @param string
	 * @return
	 */
	public static byte[] stringToByteArray(String string) {
		return string != null ? string.getBytes(StandardCharsets.US_ASCII) : null;
	}

	/**
	 * Transform a long into a hexadecimal coded byte array
	 * 
	 * @param string
	 * @return
	 */
	public static byte[] longToByteArray(long longValue) {
		return ReaderTools.hexStringToByteArray(StringUtils.leftPad(Long.toHexString(longValue), 12, "0"));
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * Transform a hexadecimal coded byte array into a string
	 * 
	 * @param bytes
	 *            hexadecimal coded byte array
	 * @return the string
	 */
	public static String byteArrayToAsciiString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < bytes.length; i++) {
			sb.append((char) Integer.parseInt(Byte.toString(bytes[i])));
		}
		return sb.toString();
	}
}