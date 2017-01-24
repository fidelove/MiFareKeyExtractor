package com.fidel;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.fidel.tools.ReaderTools;

public class FidelTest {

	@Test
	public void test() {
		// Long longValue = Long.decode("0xFFFFFFFFFFFF");
		Long longValue = 10L;
		String hexString = StringUtils.leftPad(Long.toHexString(longValue), 12, "0");

		System.out.println(longValue);
		System.out.println(hexString);
		ReaderTools.printByteArray(ReaderTools.hexStringToByteArray(hexString));

		Long j = 250000000L;
		System.out.println((double) j / Long.decode("0xFFFFFFFFFFFF"));
		// System.out.println(String.format("Checked %.0f %%%n", 0.01 * j /
		// Long.decode("0xFFFFFFFFFFFF")));

	}
}
