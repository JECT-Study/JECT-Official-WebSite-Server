package org.ject.support.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JobFamilyTest {

    @Test
    @DisplayName("프로덕트 매니저 직군을 제공한다")
    void 프로덕트_매니저_직군을_제공한다() {
        assertThat(JobFamily.PM.getDescription()).isEqualTo("프로덕트 매니저(PM)");
        assertThat(JobFamily.PM.isPortfolioRequired()).isFalse();
    }

    @Test
    @DisplayName("프로덕트 디자이너 직군을 제공한다")
    void 프로덕트_디자이너_직군을_제공한다() {
        assertThat(JobFamily.PD.getDescription()).isEqualTo("프로덕트 디자이너(PD)");
    }

    @Test
    @DisplayName("프론트엔드 개발자 직군을 제공한다")
    void 프론트엔드_개발자_직군을_제공한다() {
        assertThat(JobFamily.FE.getDescription()).isEqualTo("프론트엔드 개발자(FE)");
        assertThat(JobFamily.FE.isPortfolioRequired()).isFalse();
    }

    @Test
    @DisplayName("백엔드 개발자 직군을 제공한다")
    void 백엔드_개발자_직군을_제공한다() {
        assertThat(JobFamily.BE.getDescription()).isEqualTo("백엔드 개발자(BE)");
        assertThat(JobFamily.BE.isPortfolioRequired()).isFalse();
    }

    @Test
    @DisplayName("앱 개발자 직군을 제공한다")
    void 앱_개발자_직군을_제공한다() {
        assertThat(JobFamily.APP.getDescription()).isEqualTo("앱 개발자(APP)");
        assertThat(JobFamily.APP.isPortfolioRequired()).isFalse();
    }

    @Test
    @DisplayName("운영 서포터즈 직군을 제공한다")
    void 운영_서포터즈_직군을_제공한다() {
        assertThat(JobFamily.SUPPORTER.getDescription()).isEqualTo("운영 서포터즈");
        assertThat(JobFamily.SUPPORTER.isPortfolioRequired()).isFalse();
    }

    @Test
    @DisplayName("프로덕트 디자이너는 포트폴리오가 필수다")
    void 프로덕트_디자이너는_포트폴리오가_필수다() {
        assertThat(JobFamily.PD.isPortfolioRequired()).isTrue();
    }
}
