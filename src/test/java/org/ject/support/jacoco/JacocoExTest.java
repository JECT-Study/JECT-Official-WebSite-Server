package org.ject.support.jacoco;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JacocoExTest {

    @Test
    void jacoco_hello_world_test() {
        // given
        JacocoEx jacocoEx = new JacocoEx();

        // when
        String result = jacocoEx.helloWorld("hello");

        // then
        assertThat(result).isEqualTo("world");

    }

}