package com.fidel;

import java.util.Date;

import com.fidel.exception.ReaderException;
import com.fidel.reader.ElatecReaderForMiFareClassic;

public class MiFareKeyExtractorRunner {

	public static void main(String[] args) {

		try {

			ElatecReaderForMiFareClassic reader = ElatecReaderForMiFareClassic.getInstance();
			reader.initChannel();

			while (true) {
				int huntCard = reader.huntCard();
				if (huntCard > 0) {

					for (int i = 4; i < 64; i++) {

						for (long j = 0; j <= Long.decode("0xFFFFFFFFFFFF"); j++) {
							if (j % 100 == 0) {

								if (j != 0) {
									for (int space = 0; space < printStatus(i, j - 100).length(); space++) {
										System.out.print("\b");
									}
								}

								System.out.print(printStatus(i, j));
							}

							if (reader.loginIntoBlockWithKey(i, j)) {
								System.out.print(printStatus(i, j));
							}
						}
					}
				}
			}

		} catch (ReaderException e) {
			System.out.println(e.getMessage());
		}
	}

	private static String printStatus(int i, long j) {
		return "Checked " + j + "/" + Long.decode("0xFFFFFFFFFFFF") + " in block " + i + " at " + new Date().toString();
	}
}