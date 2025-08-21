package org.ject.support.common.util;

import java.security.SecureRandom;
import java.util.stream.Collectors;

public class CodeGeneratorUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int DIGIT_BOUND = 10;

    public static String generateAuthCode(int length) {
        return SECURE_RANDOM.ints(length, 0, DIGIT_BOUND)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }
}
