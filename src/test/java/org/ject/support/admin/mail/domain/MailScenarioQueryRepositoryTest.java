package org.ject.support.admin.mail.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.time.LocalDateTime;
import java.util.List;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Import({QueryDslTestConfig.class, MailScenarioQueryRepositoryImpl.class})
@DataJpaTest
class MailScenarioQueryRepositoryTest {

    @Autowired
    private MailScenarioRepository mailScenarioRepository;

    @Test
    void 구분과_타입으로_메일_템플릿을_필터링한다() {
        // given
        mailScenarioRepository.saveAll(List.of(
                scenario("일반 구성원 불합격", MailScenarioCategory.CLUB_MEMBER, MailScenarioType.REJECT, 1),
                scenario("일반 구성원 합격", MailScenarioCategory.CLUB_MEMBER, MailScenarioType.FIRST_PASS, 2),
                scenario("메이커스 불합격", MailScenarioCategory.MAKERS, MailScenarioType.REJECT, 3)
        ));

        // when
        Page<MailScenario> categoryResult = mailScenarioRepository.findScenarios(
                MailScenarioCategory.CLUB_MEMBER, null, PageRequest.of(0, 10));
        Page<MailScenario> typeResult = mailScenarioRepository.findScenarios(
                null, MailScenarioType.REJECT, PageRequest.of(0, 10));
        Page<MailScenario> combinedResult = mailScenarioRepository.findScenarios(
                MailScenarioCategory.CLUB_MEMBER, MailScenarioType.REJECT, PageRequest.of(0, 10));

        // then
        assertThat(categoryResult.getContent()).extracting(MailScenario::getName)
                .containsExactlyInAnyOrder("일반 구성원 불합격", "일반 구성원 합격");
        assertThat(typeResult.getContent()).extracting(MailScenario::getName)
                .containsExactlyInAnyOrder("일반 구성원 불합격", "메이커스 불합격");
        assertThat(combinedResult.getContent()).extracting(MailScenario::getName)
                .containsExactly("일반 구성원 불합격");
    }

    @Test
    void 등록일_내림차순으로_페이지를_조회한다() {
        // given
        mailScenarioRepository.saveAll(List.of(
                scenario("첫 번째", MailScenarioCategory.GENERAL, MailScenarioType.ETC, 1),
                scenario("두 번째", MailScenarioCategory.GENERAL, MailScenarioType.ETC, 2),
                scenario("세 번째", MailScenarioCategory.GENERAL, MailScenarioType.ETC, 3)
        ));

        // when
        Page<MailScenario> result = mailScenarioRepository.findScenarios(
                null, null, PageRequest.of(0, 2));

        // then
        assertThat(result.getContent()).extracting(MailScenario::getName)
                .containsExactly("세 번째", "두 번째");
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
    }

    private MailScenario scenario(String name,
                                  MailScenarioCategory category,
                                  MailScenarioType type,
                                  int day) {
        MailScenario scenario = MailScenario.builder()
                .name(name)
                .category(category)
                .type(type)
                .scenarioCode(name)
                .subjectTemplate("제목")
                .bodyTemplate("본문")
                .active(true)
                .build();
        setField(scenario, "createdAt", LocalDateTime.of(2026, 1, day, 0, 0));
        return scenario;
    }
}
