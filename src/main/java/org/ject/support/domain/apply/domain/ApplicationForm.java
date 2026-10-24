package org.ject.support.domain.apply.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.support.domain.base.BaseTimeEntity;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationForm extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "mediumtext")
    private String content;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apply_id", nullable = false)
    private Apply apply;

    @OneToMany(mappedBy = "applicationForm", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence asc")
    @Builder.Default
    private List<Portfolio> portfolios = new ArrayList<>();

    public void updateContentAndPortfolios(String newContent, List<Portfolio> newPortfolios) {
        this.content = newContent;
        this.portfolios.clear();
        this.portfolios.addAll(newPortfolios);
        newPortfolios.forEach(portfolio -> portfolio.setApplicationForm(this));
    }

    // 기존 포트폴리오는 유지하고 질문별 답변만 편집
    public void editContent(String content) {
        this.content = content;
    }

    // 현재 지원서 소유 항목인지 검증한 뒤 요청한 포트폴리오만 삭제
    public void deletePortfolios(Set<Long> portfolioIds) {
        Set<Long> existingIds = portfolios.stream().map(Portfolio::getId).collect(Collectors.toSet());
        if (!existingIds.containsAll(portfolioIds)) {
            throw new ApplyException(ApplyErrorCode.NOT_FOUND_PORTFOLIO);
        }
        portfolios.removeIf(portfolio -> portfolioIds.contains(portfolio.getId()));
    }

    public boolean hasNoPortfolio() {
        return portfolios == null || portfolios.isEmpty();
    }
}
