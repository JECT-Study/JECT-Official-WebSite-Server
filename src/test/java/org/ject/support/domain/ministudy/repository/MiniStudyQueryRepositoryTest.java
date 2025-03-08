package org.ject.support.domain.ministudy.repository;

import org.ject.support.domain.ministudy.dto.MiniStudyResponse;
import org.ject.support.domain.ministudy.entity.MiniStudy;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@Transactional
class MiniStudyQueryRepositoryTest {

    @Autowired
    private MiniStudyRepository miniStudyRepository;

    @Test
    @DisplayName("미니 스터디 목록 조회 - 페이징")
    void findMiniStudies() {
        // given
        MiniStudy miniStudy1 = createMiniStudy("미니 스터디 1");
        MiniStudy miniStudy2 = createMiniStudy("미니 스터디 2");
        MiniStudy miniStudy3 = createMiniStudy("미니 스터디 3");
        miniStudyRepository.saveAll(List.of(miniStudy1, miniStudy2, miniStudy3));

        // when
        Page<MiniStudyResponse> result = miniStudyRepository.findMiniStudies(PageRequest.of(0, 2));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(3); // 전체 데이터 개수
        assertThat(result.getTotalPages()).isEqualTo(2); // 전체 페이지 수
        assertThat(result.getNumber()).isEqualTo(0); // 현재 페이지 번호
        assertThat(result.getSize()).isEqualTo(2); // 페이지 크기

        List<MiniStudyResponse> responses = result.getContent();
        assertThat(responses).hasSize(2); // 현재 페이지의 데이터 개수

        MiniStudyResponse firstResponse = responses.get(0);
        assertThat(firstResponse.id()).isNotNull();
        assertThat(firstResponse.name()).isEqualTo("미니 스터디 3"); // ID 내림차순이므로 마지막에 생성된 데이터가 첫 번째
        assertThat(firstResponse.summary()).isEqualTo("summary");
        assertThat(firstResponse.linkUrl()).isEqualTo("https://test.net/ministudy3");
        assertThat(firstResponse.imageUrl()).isEqualTo("https://test.net/image3.png");

        // 두 번째 페이지 조회
        Page<MiniStudyResponse> secondPage = miniStudyRepository.findMiniStudies(PageRequest.of(1, 2));
        assertThat(secondPage.getContent()).hasSize(1); // 마지막 페이지는 1개의 데이터만 존재
    }

    private MiniStudy createMiniStudy(String name) {
        String urlSafeName = name.replaceAll("[미니 스터디]", "");
        return MiniStudy.builder()
                .name(name)
                .summary("summary")
                .linkUrl("https://test.net/ministudy" + urlSafeName)
                .imageUrl("https://test.net/image" + urlSafeName + ".png")
                .build();
    }
}
