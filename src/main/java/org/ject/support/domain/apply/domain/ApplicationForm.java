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

import java.util.ArrayList;
import java.util.List;

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

    public boolean hasNoPortfolio() {
        return portfolios == null || portfolios.isEmpty();
    }
}
