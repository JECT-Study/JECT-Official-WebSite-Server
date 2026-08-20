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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.base.BaseTimeEntity;
import org.ject.support.domain.recruit.domain.Recruit;

@Entity
@Getter
@Builder
@Table(name = "apply", uniqueConstraints = @UniqueConstraint(
        name = "uk_apply_recruit_waitlist_number",
        columnNames = {"recruit_id", "waitlist_number"}))
@SQLDelete(sql = "UPDATE apply SET is_deleted = true, selection_result = 'UNDECIDED', waitlist_number = NULL, "
        + "version = version + 1 "
        + "WHERE id = ? AND version = ?")
@SQLRestriction("is_deleted = false")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Apply extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Applicant applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_id", nullable = false)
    private Recruit recruit;

    @OneToOne(mappedBy = "apply", cascade = CascadeType.ALL, orphanRemoval = true)
    private ApplicationForm applicationForm;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)", nullable = false)
    private ApplyStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "selection_result", columnDefinition = "varchar(50)", nullable = false)
    @Builder.Default
    private SelectionResult selectionResult = SelectionResult.UNDECIDED;

    @Column(name = "waitlist_number")
    private Integer waitlistNumber;

    @Column(columnDefinition = "varchar(500) default ''", nullable = false)
    @Builder.Default
    private String note = "";

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Version
    private Long version;

    public static Apply createApply(Applicant applicant, Recruit recruit) {
        return Apply.builder()
                .applicant(applicant)
                .recruit(recruit)
                .status(ApplyStatus.JOINED)
                .build();
    }

    public void updateApplicationForm(ApplicationForm newApplicationForm) {
        this.applicationForm = newApplicationForm;
    }

    public void saveTemporarily() {
        this.status = ApplyStatus.TEMP_SAVED;
    }

    public void resetToJoined() {
        this.status = ApplyStatus.JOINED;
    }

    public boolean isNotTempSaved() {
        return status.equals(ApplyStatus.JOINED)
                || status.equals(ApplyStatus.SUBMITTED)
                || (status.equals(ApplyStatus.TEMP_SAVED) && applicationForm == null);
    }

    public boolean isNotSubmitted() {
        return status.equals(ApplyStatus.JOINED)
                || status.equals(ApplyStatus.TEMP_SAVED)
                || status.equals(ApplyStatus.REJECTED)
                || (status.equals(ApplyStatus.SUBMITTED) && applicationForm == null);
    }

    public void deleteApplicationForm() {
        if (applicationForm != null) {
            applicationForm = null;
        }
    }

    public void reject() {
        this.applicationForm = null;
        this.status = ApplyStatus.REJECTED;
        this.selectionResult = SelectionResult.UNDECIDED;
        this.waitlistNumber = null;
    }

    public boolean isTempSaved() {
        return status.equals(ApplyStatus.TEMP_SAVED);
    }

    public boolean isSubmitted() {
        return status.equals(ApplyStatus.SUBMITTED);
    }

    /**
     * 선정 결과를 정한다. 지원 상태와는 별도의 축이므로 상태나 지원서는 건드리지 않는다.
     */
    public void decideSelectionResult(SelectionResult selectionResult, Integer waitlistNumber) {
        validateSelectionResult(selectionResult, waitlistNumber);

        this.selectionResult = selectionResult;
        this.waitlistNumber = waitlistNumber;
    }

    public void validateSelectionResult(SelectionResult selectionResult, Integer waitlistNumber) {
        if (isNotSubmitted()) {
            throw new ApplyException(ApplyErrorCode.NOT_SUBMITTED);
        }

        if (selectionResult.isWaitlisted() && waitlistNumber == null) {
            throw new ApplyException(ApplyErrorCode.WAITLIST_NUMBER_REQUIRED);
        }

        if (selectionResult.isWaitlisted() && waitlistNumber <= 0) {
            throw new ApplyException(ApplyErrorCode.INVALID_WAITLIST_NUMBER);
        }

        if (!selectionResult.isWaitlisted() && waitlistNumber != null) {
            throw new ApplyException(ApplyErrorCode.WAITLIST_NUMBER_NOT_ALLOWED);
        }
    }

    public void clearWaitlistNumber() {
        this.waitlistNumber = null;
    }

    public void submit(ApplicationForm applicationForm) {
        if (isSubmitted()) {
            throw new ApplyException(ApplyErrorCode.ALREADY_SUBMITTED);
        }

        if (recruit.getJobFamily().isPortfolioRequired() && applicationForm.hasNoPortfolio()) {
            throw new ApplyException(ApplyErrorCode.PORTFOLIO_REQUIRED);
        }

        this.applicationForm = applicationForm;
        this.status = ApplyStatus.SUBMITTED;
    }

}
