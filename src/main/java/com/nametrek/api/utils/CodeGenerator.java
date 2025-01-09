package com.nametrek.api.utils;

import java.security.SecureRandom;
import java.util.Random;

public class CodeGenerator {
	private static final String alphabet = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
	private static final int CODE_LENGTH = 8;
	private static final Random random = new SecureRandom();


	public static String generateCode() {
		StringBuilder code = new StringBuilder(CODE_LENGTH);

		for (int i = 0; i < CODE_LENGTH; i++) {
			code.append(alphabet.charAt(random.nextInt(alphabet.length())));
		}
		return code.toString();
	}
}
