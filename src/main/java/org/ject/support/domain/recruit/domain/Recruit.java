package org.ject.support.domain.recruit.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.ject.support.domain.base.BaseTimeEntity;
import org.ject.support.domain.member.JobFamily;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(RecruitEntityListener.class)
public class Recruit extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_family", columnDefinition = "varchar(45)", nullable = false)
    private JobFamily jobFamily;

    @Column(name = "is_closed", nullable = false)
    @Builder.Default
    private boolean isClosed = false;

    @OneToMany(mappedBy = "recruit", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    public void addQuestion(Question question) {
        this.questions.add(question);
        question.setRecruit(this);
    }

    /**
     * @return 지원 `기한`인지
     */
    public Boolean isRecruitingPeriod() {
        LocalDateTime now = LocalDateTime.now();
        return startDate.isBefore(now) && endDate.isAfter(now);
    }

    public boolean isInvalidQuestionId(final Long questionId) {
        return questions.stream().noneMatch(question -> question.getId().equals(questionId));
    }

    public void update(JobFamily jobFamily, LocalDateTime startDate, LocalDateTime endDate) {
        this.jobFamily = jobFamily;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
