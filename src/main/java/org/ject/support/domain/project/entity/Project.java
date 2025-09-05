package org.ject.support.domain.project.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.support.common.util.StringListConverter;
import org.ject.support.domain.base.BaseTimeEntity;
import org.ject.support.domain.member.entity.Team;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(30)", nullable = false)
    private Project.Category category;

    @Column(length = 100, nullable = false)
    private String summary;

    @Column
    @Convert(converter = StringListConverter.class)
    @Builder.Default
    private List<String> techStack = new ArrayList<>();

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 2083)
    private String thumbnailUrl;

    @Column(length = 2083)
    private String serviceUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    @OrderBy("sequence asc")
    @Builder.Default
    private List<ProjectIntro> projectIntros = new ArrayList<>();

    public enum Category {
        MAIN, HACKATHON
    }
}
