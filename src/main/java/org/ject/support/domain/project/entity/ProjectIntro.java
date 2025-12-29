package org.ject.support.domain.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.base.BaseTimeEntity;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectIntro extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2083, nullable = false)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(30)", nullable = false)
    private Category category;

    @Column
    private Integer sequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public boolean isCategory(Category category) {
        return this.category == category;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Category {
        BANNER("배너 이미지"),
        SAMPLE("샘플 이미지"),
        DESCRIPTION("소개 이미지");

        private final String description;
    }
}
