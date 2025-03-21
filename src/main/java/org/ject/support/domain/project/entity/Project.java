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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.support.domain.base.BaseTimeEntity;
import org.ject.support.domain.member.entity.Team;

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

    @Column(length = 20, nullable = false)
    private String semester;

    @Column(length = 100, nullable = false)
    private String summary;

    @Column
    private String techStack;

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

    public List<String> getTechStack() {
        return Arrays.stream(techStack.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public enum Category {
        MAIN, HACKATHON
    }
}
