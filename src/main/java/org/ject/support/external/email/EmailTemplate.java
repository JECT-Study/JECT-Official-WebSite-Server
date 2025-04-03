package org.ject.support.external.email;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.exception.GlobalErrorCode;
import org.ject.support.common.exception.GlobalException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

@Getter
@AllArgsConstructor
public enum EmailTemplate {
    ACCEPTANCE("email-template", "ject notification"),
    REJECTION("email-template", "ject notification"),
    CERTIFICATE("email-template", "ject certificate"),
    PIN_RESET("email-template", "ject pin reset"); // TODO: HTML 템플릿별로 수정 필요

    private final String templateName;// template file name
    private final String subject;// email subject

    @PostConstruct
    private void validateFileName() throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<String> templateFileNames = Arrays.stream(values()).map(EmailTemplate::getTemplateName).toList();

        Set<String> actualFileNames = Arrays.stream(resolver.getResources("classpath:/templates/*.html"))
                .map(Resource::getFilename)
                .collect(Collectors.toSet());

        for (String templateFileName : templateFileNames) {
            if (!actualFileNames.contains(templateFileName + ".html")) {
                throw new GlobalException(GlobalErrorCode.TEMPLATE_NOT_FOUND);
            }
        }
    }
}
