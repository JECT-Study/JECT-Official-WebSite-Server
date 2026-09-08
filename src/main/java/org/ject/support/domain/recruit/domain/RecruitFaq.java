package org.ject.support.domain.recruit.domain;

public record RecruitFaq(
        String title,
        String content
) {

    // 모집공고 FAQ 생성
    public static RecruitFaq create(String title, String content) {
        return new RecruitFaq(title, content);
    }
}
