package org.ject.support.domain.member;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JobFamilyTest {

    @Test
    void 운영_서포터즈_직군을_제공한다() {
        assertThat(JobFamily.SUPPORTER.getDescription()).isEqualTo("운영 서포터즈");
        assertThat(JobFamily.SUPPORTER.isPortfolioRequired()).isFalse();
    }
}
