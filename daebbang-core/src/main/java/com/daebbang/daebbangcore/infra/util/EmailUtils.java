package com.daebbang.daebbangcore.infra.util;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.CommonErrorCode;
import java.security.SecureRandom;

public class EmailUtils {
    private EmailUtils() {
        throw new BusinessException(CommonErrorCode.CANNOT_INSTANTIATE_UTIL_CLASS);
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String CHAR_LOWER = "abcdefghijkmnopqrstuvwxyz";
    private static final String CHAR_UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String NUMBER = "23456789";
    private static final String SPECIAL_CHAR = "!@#$%^&*";

    private static final String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL_CHAR;

    public static String generateTemporaryPassword() {
        StringBuilder sb = new StringBuilder(12);

        sb.append(CHAR_LOWER.charAt(RANDOM.nextInt(CHAR_LOWER.length())));
        sb.append(CHAR_UPPER.charAt(RANDOM.nextInt(CHAR_UPPER.length())));
        sb.append(NUMBER.charAt(RANDOM.nextInt(NUMBER.length())));
        sb.append(SPECIAL_CHAR.charAt(RANDOM.nextInt(SPECIAL_CHAR.length())));

        for (int i = 0; i < 8; i++) {
            sb.append(PASSWORD_ALLOW_BASE.charAt(RANDOM.nextInt(PASSWORD_ALLOW_BASE.length())));
        }

        return shuffleString(sb.toString());
    }

    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }
}
