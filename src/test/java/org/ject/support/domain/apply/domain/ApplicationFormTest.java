package org.ject.support.domain.apply.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.ject.support.base.TestSupport;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ApplicationFormTest extends TestSupport {

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void 요청한_포트폴리오만_삭제한다(boolean delete) {
        // given
        Portfolio first = Portfolio.builder().id(1L).fileUrl("first.pdf").build();
        Portfolio second = Portfolio.builder().id(2L).fileUrl("second.pdf").build();
        ApplicationForm form = ApplicationForm.builder()
                .portfolios(new ArrayList<>(List.of(first, second))).build();

        // when
        form.deletePortfolios(delete ? Set.of(1L) : Set.of());

        // then
        assertThat(form.getPortfolios()).containsExactlyElementsOf(delete ? List.of(second) : List.of(first, second));
    }

    @Test
    void 다른_지원서의_포트폴리오가_포함되면_삭제를_거부한다() {
        // given
        Portfolio owned = Portfolio.builder().id(1L).build();
        ApplicationForm form = ApplicationForm.builder().portfolios(new ArrayList<>(List.of(owned))).build();
        ApplicationForm otherForm = ApplicationForm.builder()
                .portfolios(new ArrayList<>(List.of(Portfolio.builder().id(2L).build()))).build();
        Set<Long> ids = Set.of(owned.getId(), otherForm.getPortfolios().getFirst().getId());

        // when, then
        assertThatThrownBy(() -> form.deletePortfolios(ids)).isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_PORTFOLIO);
        assertThat(form.getPortfolios()).containsExactly(owned);
    }
}
