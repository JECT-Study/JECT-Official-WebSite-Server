package org.ject.support.domain.project.dto;

import org.ject.support.domain.project.entity.Project;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public record ProjectSummaryResponse(
        List<CategorySummary> categorySummaries
) {

    public static ProjectSummaryResponse of(List<Project> projects) {

        Map<Project.Category, Long> categoryCountMap = new EnumMap<>(Project.Category.class);

        for (Project.Category category : Project.Category.values()) {
            categoryCountMap.put(category, 0L);
        }

        for (Project project : projects) {
            Project.Category category = project.getCategory();
            categoryCountMap.put(
                    category,
                    categoryCountMap.get(category) + 1
            );
        }

        List<CategorySummary> summaries = new ArrayList<>();

        summaries.add(new CategorySummary(
                "ALL",
                (long) projects.size()
        ));

        for (Project.Category category : Project.Category.values()) {
            summaries.add(new CategorySummary(
                    category.name(),
                    categoryCountMap.get(category)
            ));
        }

        return new ProjectSummaryResponse(summaries);
    }

    public record CategorySummary(
            String categoryName,
            Long count
    ) {
    }
}
