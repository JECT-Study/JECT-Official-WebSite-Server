package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import org.ject.support.admin.mail.dto.MailTargetResponse;
import org.ject.support.admin.mail.repository.MailTargetQueryRepository;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MailTargetServiceTest {

    @InjectMocks
    private MailTargetService mailTargetService;

    @Mock
    private RecruitRepository recruitRepository;

    @Mock
    private MailTargetQueryRepository mailTargetQueryRepository;

    @Test
    void 모집_공고와_선정_결과로_메일_발송_대상을_조회한다() {
        // given
        Long recruitId = 1L;
        MailTargetResponse response = new MailTargetResponse(
                10L, "홍길동", "01012345678", "test@test.com", SelectionResult.PASSED, null);
        given(recruitRepository.existsById(recruitId)).willReturn(true);
        given(mailTargetQueryRepository.findTargets(recruitId, SelectionResult.PASSED))
                .willReturn(List.of(response));

        // when
        List<MailTargetResponse> result = mailTargetService.getTargets(recruitId, SelectionResult.PASSED);

        // then
        assertThat(result).containsExactly(response);
    }

    @Test
    void 존재하지_않는_모집_공고면_예외가_발생한다() {
        // given
        Long recruitId = 999L;
        given(recruitRepository.existsById(recruitId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> mailTargetService.getTargets(recruitId, null))
                .isInstanceOf(RecruitException.class)
                .extracting("errorCode")
                .isEqualTo(RecruitErrorCode.NOT_FOUND_RECRUIT);
        then(mailTargetQueryRepository).shouldHaveNoInteractions();
    }
}
