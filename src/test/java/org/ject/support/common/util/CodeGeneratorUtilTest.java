package org.ject.support.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeGeneratorUtilTest {

    @Test
    @DisplayName("랜덤 인증 숫자 코드 생성 테스트")
    void generateDigitCode_length_check() {
        // given
        int length = 6;

        // when
        String code = CodeGeneratorUtil.generateDigitCode(length);

        // then
        assertNotNull(code);
        assertEquals(length, code.length());
        assertTrue(code.matches("[0-9]+"));
    }

    @Test
    @DisplayName("랜덤 인증 코드(숫자+대문자) 생성 테스트")
    void generateAlphaNumCode_length_check() {
        // given
        int length = 6;

        // when
        String code = CodeGeneratorUtil.generateAlphaNumCode(length);

        for (int i = 0; i < 50; i++) {
            System.out.println(CodeGeneratorUtil.generateAlphaNumCode(length));
        }

        // then
        assertNotNull(code);
        assertEquals(length, code.length());
        assertTrue(code.matches("[0-9A-Z]+"));
    }
}
