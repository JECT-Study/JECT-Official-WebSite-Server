package org.ject.support.common.util;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CodeGeneratorUtilTest {

    @Test
    void 길이가_6인_랜덤한_숫자_생성을_요청하면_6자리_숫자_코드가_생성된다() {
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
    void 길이가_6인_랜덤한_알파벳과_숫자_생성을_요청하면_6자리_코드가_생성된다() {
        // given
        int length = 6;

        // when
        String code = CodeGeneratorUtil.generateUpperAlphaNumCode(length);

        // then
        assertNotNull(code);
        assertEquals(length, code.length());
        assertTrue(code.matches("[0-9A-Z]+"));
    }
}
