package org.ject.support.admin.mail.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.support.domain.base.BaseTimeEntity;

/**
 * 메일 발송 시나리오 정보를 저장하는 엔티티입니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mail_scenario")
public class MailScenario extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MailScenarioCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MailScenarioType type;

    @Column(name = "scenario_code", nullable = false, unique = true)
    private String scenarioCode;

    @Column(name = "subject_template", nullable = false, columnDefinition = "TEXT")
    private String subjectTemplate;

    @Column(name = "body_template", nullable = false, columnDefinition = "TEXT")
    private String bodyTemplate;

    @Column(nullable = false)
    private boolean active;

    @ElementCollection
    @CollectionTable(
            name = "mail_scenario_variables",
            joinColumns = @JoinColumn(name = "mail_scenario_id")
    )
    private Set<MailScenarioVariable> customVariables = new HashSet<>();

    @Builder
    public MailScenario(String name,
                        MailScenarioCategory category,
                        MailScenarioType type,
                        String scenarioCode,
                        String subjectTemplate,
                        String bodyTemplate,
                        Boolean active,
                        Set<MailScenarioVariable> customVariables) {
        this.name = name;
        this.category = category;
        this.type = type;
        this.scenarioCode = scenarioCode;
        this.subjectTemplate = subjectTemplate;
        this.bodyTemplate = bodyTemplate;
        this.active = active != null ? active : true;
        this.customVariables = customVariables != null ? customVariables : new HashSet<>();
    }

    /**
     * 시나리오의 기본 정보와 템플릿/변수 구성을 갱신합니다.
     */
    public void update(String name,
                       MailScenarioCategory category,
                       MailScenarioType type,
                       String scenarioCode,
                       String subjectTemplate,
                       String bodyTemplate,
                       Boolean active,
                       Set<MailScenarioVariable> customVariables) {
        this.name = name;
        this.category = category;
        this.type = type;
        this.scenarioCode = scenarioCode;
        this.subjectTemplate = subjectTemplate;
        this.bodyTemplate = bodyTemplate;
        this.active = active != null ? active : this.active;
        this.customVariables = customVariables != null ? customVariables : new HashSet<>();
    }
}
