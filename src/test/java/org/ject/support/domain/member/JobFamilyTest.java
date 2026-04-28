package org.ject.support.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JobFamilyTest {

    @Test
    @DisplayName("운영 서포터즈 직군을 제공한다")
    void provide_supporter_job_family() {
        assertThat(JobFamily.SUPPORTER.getDescription()).isEqualTo("운영 서포터즈");
        assertThat(JobFamily.SUPPORTER.isPortfolioRequired()).isFalse();
    }
}
