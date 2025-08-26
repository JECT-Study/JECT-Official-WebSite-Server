package org.ject.support.common.util;

import java.security.SecureRandom;
import java.util.stream.Collectors;

public class CodeGeneratorUtil {

    private CodeGeneratorUtil() {
        throw new UnsupportedOperationException("인스턴스화 방지");
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String DIGITS = "0123456789";
    private static final String DIGITS_AND_UPPERCASE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // 숫자만 랜덤 코드 생성
    public static String generateDigitCode(int length) {
        return SECURE_RANDOM.ints(length, 0, DIGITS.length())
                .mapToObj(i -> String.valueOf(DIGITS.charAt(i)))
                .collect(Collectors.joining());
    }

    // 숫자 + 대문자 랜덤 코드 생성
    public static String generateUpperAlphaNumCode(int length) {
        return SECURE_RANDOM.ints(length, 0, DIGITS_AND_UPPERCASE.length())
                .mapToObj(i -> String.valueOf(DIGITS_AND_UPPERCASE.charAt(i)))
                .collect(Collectors.joining());
    }
}
