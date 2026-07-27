package org.ject.support.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

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
    @DisplayName("운영팀 직군을 제공한다")
    void 운영팀_직군을_제공한다() {
        assertThat(JobFamily.OPS.getDescription()).isEqualTo("운영팀(OPS)");
        assertThat(JobFamily.OPS.isPortfolioRequired()).isFalse();
    }

    @Test
    @DisplayName("인프라팀 직군을 제공한다")
    void 인프라팀_직군을_제공한다() {
        assertThat(JobFamily.INFRA.getDescription()).isEqualTo("인프라팀(INFRA)");
        assertThat(JobFamily.INFRA.isPortfolioRequired()).isFalse();
    }

    @Test
    @DisplayName("BX팀 직군을 제공한다")
    void BX팀_직군을_제공한다() {
        assertThat(JobFamily.BX.getDescription()).isEqualTo("BX팀(BX)");
        assertThat(JobFamily.BX.isPortfolioRequired()).isFalse();
    }

    @Test
    @DisplayName("대외협력팀 직군을 제공한다")
    void 대외협력팀_직군을_제공한다() {
        assertThat(JobFamily.ER.getDescription()).isEqualTo("대외협력팀(ER)");
        assertThat(JobFamily.ER.isPortfolioRequired()).isFalse();
    }

    @Test
    @DisplayName("프로덕트 디자이너는 포트폴리오가 필수다")
    void 프로덕트_디자이너는_포트폴리오가_필수다() {
        assertThat(JobFamily.PD.isPortfolioRequired()).isTrue();
    }

    @Test
    @DisplayName("정규 기수에서 사용할 수 있는 직군을 반환한다")
    void 정규_기수에서_사용할_수_있는_직군을_반환한다() {
        assertThat(availableJobFamilies(MemberType.SEMESTER))
            .containsExactlyInAnyOrder(JobFamily.PM, JobFamily.PD, JobFamily.FE, JobFamily.BE, JobFamily.APP);
    }

    @Test
    @DisplayName("메이커스에서 사용할 수 있는 직군을 반환한다")
    void 메이커스에서_사용할_수_있는_직군을_반환한다() {
        assertThat(availableJobFamilies(MemberType.MAKERS))
            .containsExactlyInAnyOrder(JobFamily.PM, JobFamily.PD, JobFamily.FE, JobFamily.BE);
    }

    @Test
    @DisplayName("운영 서포터즈에서 사용할 수 있는 직군을 반환한다")
    void 운영_서포터즈에서_사용할_수_있는_직군을_반환한다() {
        assertThat(availableJobFamilies(MemberType.SUPPORTERS))
            .containsExactlyInAnyOrder(JobFamily.OPS, JobFamily.INFRA, JobFamily.BX, JobFamily.ER);
    }

    private JobFamily[] availableJobFamilies(MemberType memberType) {
        return Arrays.stream(JobFamily.values())
            .filter(jobFamily -> jobFamily.isAvailableFor(memberType))
            .toArray(JobFamily[]::new);
    }
}
