package org.ject.support.domain.apply.domain;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.base.BaseTimeEntity;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.recruit.domain.Recruit;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Apply extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_id", nullable = false)
    private Recruit recruit;

    @OneToOne(mappedBy = "apply", cascade = CascadeType.ALL, orphanRemoval = true)
    private ApplicationForm applicationForm;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)", nullable = false)
    private Status status;

    @Column(columnDefinition = "varchar(500) default ''", nullable = false)
    @Builder.Default
    private String note = "";

    public static Apply createApply(Member member, Recruit recruit) {
        return Apply.builder()
                .member(member)
                .recruit(recruit)
                .status(Status.JOINED)
                .build();
    }

    public void updateApplicationForm(ApplicationForm newApplicationForm) {
        this.applicationForm = newApplicationForm;
    }

    public void updateNote(String note) {
        this.note = note != null ? note : "";
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    public boolean isNotTempSaved() {
        return status.equals(Status.JOINED)
                || status.equals(Status.SUBMITTED)
                || (status.equals(Status.TEMP_SAVED) && applicationForm == null);
    }

    public boolean isNotSubmitted() {
        return status.equals(Status.JOINED)
                || status.equals(Status.TEMP_SAVED)
                || (status.equals(Status.SUBMITTED) && applicationForm == null);
    }

    public void deleteApplicationForm() {
        if (applicationForm != null) {
            applicationForm = null;
        }
    }

    public boolean isTempSaved() {
        return status.equals(Status.TEMP_SAVED);
    }

    public boolean isSubmitted() {
        return status.equals(Status.SUBMITTED);
    }

    public void submit(ApplicationForm applicationForm) {
        if (isSubmitted()) {
            throw new ApplyException(ApplyErrorCode.ALREADY_SUBMITTED);
        }

        if (recruit.getJobFamily().isPortfolioRequired() && applicationForm.hasNoPortfolio()) {
            throw new ApplyException(ApplyErrorCode.PORTFOLIO_REQUIRED);
        }

        this.applicationForm = applicationForm;
        this.status = Status.SUBMITTED;
    }

    public enum Status {
        JOINED, TEMP_SAVED, SUBMITTED
    }
}
