package org.ject.support.admin.mail.service;

import org.ject.support.admin.mail.service.MailTemplateEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MailTemplateEngineTest {

    private final MailTemplateEngine engine = new MailTemplateEngine();

    @Test
    @DisplayName("템플릿의 변수를 제공된 맵의 값으로 치환한다")
    void render_Success() {
        // given
        String template = "Hello, ${name}! Welcome to ${project}.";
        Map<String, Object> variables = Map.of(
                "name", "Antigravity",
                "project", "JECT"
        );

        // when
        String result = engine.render(template, variables);

        // then
        assertThat(result).isEqualTo("Hello, Antigravity! Welcome to JECT.");
    }

    @Test
    @DisplayName("템플릿이 null이거나 빈 문자열이면 빈 문자열을 반환한다")
    void render_EmptyTemplate() {
        assertThat(engine.render(null, Map.of())).isEqualTo("");
        assertThat(engine.render("", Map.of())).isEqualTo("");
        assertThat(engine.render("  ", Map.of())).isEqualTo("");
    }

    @Test
    @DisplayName("매칭되는 변수가 없으면 플레이스홀더를 그대로 유지한다 (기본 동작)")
    void render_NoMatch() {
        // given
        String template = "Hello, ${name}!";
        Map<String, Object> variables = Map.of("title", "Mr.");

        // when
        String result = engine.render(template, variables);

        // then
        assertThat(result).isEqualTo("Hello, ${name}!");
    }
}
