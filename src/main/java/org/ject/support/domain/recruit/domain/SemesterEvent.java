package org.ject.support.domain.recruit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.support.domain.base.BaseTimeEntity;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "semester_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SemesterEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long semesterId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(45)", nullable = false)
    private SemesterEventType type;

    @Column(length = 25, nullable = false)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRequired = true;

    // 기수별 행사 생성
    public static SemesterEvent create(
            Long semesterId,
            SemesterEventType type,
            String name
    ) {
        return SemesterEvent.builder()
                .semesterId(semesterId)
                .type(type)
                .name(name)
                .build();
    }

    // 기수별 행사 이름 변경
    public void updateName(String name) {
        this.name = name;
    }
}
