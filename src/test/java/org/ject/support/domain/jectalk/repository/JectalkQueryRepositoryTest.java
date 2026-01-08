package org.ject.support.domain.jectalk.repository;

import java.util.List;
import org.ject.support.domain.jectalk.dto.JectalkResponse;
import org.ject.support.domain.jectalk.entity.Jectalk;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@Transactional
class JectalkQueryRepositoryTest {

    @Autowired
    private JectalkRepository jectalkRepository;

    @Test
    @DisplayName("젝톡 목록 조회 - 페이징")
    void findJectalks() {
        // given
        Jectalk jectalk1 = createJectalk("젝톡 1");
        Jectalk jectalk2 = createJectalk("젝톡 2");
        Jectalk jectalk3 = createJectalk("젝톡 3");
        jectalkRepository.saveAll(List.of(jectalk1, jectalk2, jectalk3));

        // when
        Page<JectalkResponse> result = jectalkRepository.findJectalks(PageRequest.of(0, 2));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(3); // 전체 데이터 개수
        assertThat(result.getTotalPages()).isEqualTo(2); // 전체 페이지 수
        assertThat(result.getNumber()).isEqualTo(0); // 현재 페이지 번호
        assertThat(result.getSize()).isEqualTo(2); // 페이지 크기

        List<JectalkResponse> responses = result.getContent();
        assertThat(responses).hasSize(2); // 현재 페이지의 데이터 개수
        responses.forEach(jectalkResponse -> {
            assertThat(jectalkResponse.id()).isNotNull();
            assertThat(jectalkResponse.title()).isNotNull();
            assertThat(jectalkResponse.thumbnailUrl()).isNotNull();
            assertThat(jectalkResponse.contentUrl()).isNotNull();
            assertThat(jectalkResponse.description()).isNotNull();
            assertThat(jectalkResponse.contentType()).isNotNull();
            assertThat(jectalkResponse.summary()).isNotNull();
        });

        JectalkResponse firstResponse = responses.get(0);
        assertThat(firstResponse.title()).isEqualTo("젝톡 3"); // ID 내림차순이므로 마지막에 생성된 데이터가 첫 번째
        assertThat(firstResponse.description()).isEqualTo("description");
        assertThat(firstResponse.contentUrl()).isEqualTo("https://youtube.com/jectalk3");
        assertThat(firstResponse.thumbnailUrl()).isEqualTo("https://image.com/jectalk3.png");
        assertThat(firstResponse.summary()).isEqualTo("author");


        // 두 번째 페이지 조회
        Page<JectalkResponse> secondPage = jectalkRepository.findJectalks(PageRequest.of(1, 2));
        assertThat(secondPage.getContent()).hasSize(1); // 마지막 페이지는 1개의 데이터만 존재
    }

    private Jectalk createJectalk(String name) {
        String urlSafeName = "jectalk" + name.replaceAll("[젝톡 ]", "");
        return Jectalk.builder()
                .title(name)
                .description("description")
                .contentType(org.ject.support.domain.jectalk.enums.ContentType.YOUTUBE)
                .contentUrl("https://youtube.com/" + urlSafeName)
                .thumbnailUrl("https://image.com/" + urlSafeName + ".png")
                .author("author")
                .build();
    }
}
