package org.ject.support.domain.recruit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import lombok.Setter;
import org.ject.support.common.util.StringListConverter;
import org.ject.support.domain.base.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Question extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int sequence;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private InputType inputType;

    @Column(nullable = false)
    private boolean isRequired;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 30, nullable = false)
    private String label;

    @Column
    @Convert(converter = StringListConverter.class)
    @Builder.Default
    private List<String> selectOptions = new ArrayList<>();

    @Column
    private String inputHint;

    @Column
    private Integer maxTextLength; // 최대 글자수 (공백 포함)

    @Column
    private Integer maxFileSize; // 최대 파일 크기 (MB)

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_id", nullable = false)
    private Recruit recruit;

    public enum InputType {
        SELECT, TEXT, URL, FILE
    }
}
