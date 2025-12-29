package org.ject.support.domain.project.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectIntroTest {

    @Test
    @DisplayName("프로젝트 소개서의 카테고리가 일치하는지 확인")
    void is_category() {
        // given
        ProjectIntro serviceIntro = createProjectIntro(1L, ProjectIntro.Category.SAMPLE, "image1.png");
        ProjectIntro devIntro = createProjectIntro(2L, ProjectIntro.Category.DESCRIPTION, "image2.png");

        // when, then
        assertThat(serviceIntro.isCategory(ProjectIntro.Category.SAMPLE)).isTrue();
        assertThat(serviceIntro.isCategory(ProjectIntro.Category.DESCRIPTION)).isFalse();
        assertThat(devIntro.isCategory(ProjectIntro.Category.DESCRIPTION)).isTrue();
        assertThat(devIntro.isCategory(ProjectIntro.Category.SAMPLE)).isFalse();
    }

    private ProjectIntro createProjectIntro(long id, ProjectIntro.Category dev, String image) {
        return ProjectIntro.builder()
                .id(id)
                .category(dev)
                .imageUrl(image)
                .sequence(1)
                .build();
    }
}