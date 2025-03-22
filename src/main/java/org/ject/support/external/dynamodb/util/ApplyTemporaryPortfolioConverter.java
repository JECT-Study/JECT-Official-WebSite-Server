package org.ject.support.external.dynamodb.util;

import org.ject.support.domain.recruit.dto.ApplyPortfolioDto;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ApplyTemporaryPortfolioConverter implements AttributeConverter<List<ApplyPortfolioDto>> {

    private static final String FILE_URL = "fileUrl";
    private static final String FILE_NAME = "fileName";
    private static final String FILE_SIZE = "fileSize";
    private static final String SEQUENCE = "sequence";

    @Override
    public AttributeValue transformFrom(List<ApplyPortfolioDto> portfolios) {
        return AttributeValue.builder()
                .l(portfolios.stream()
                        .map(portfolio -> AttributeValue.builder()
                                .m(Map.of(
                                        FILE_URL, AttributeValue.builder().s(portfolio.fileUrl()).build(),
                                        FILE_NAME, AttributeValue.builder().s(portfolio.fileName()).build(),
                                        FILE_SIZE, AttributeValue.builder().s(portfolio.fileSize()).build(),
                                        SEQUENCE, AttributeValue.builder().s(portfolio.sequence()).build()
                                ))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public List<ApplyPortfolioDto> transformTo(AttributeValue attributeValue) {
        return attributeValue.l().stream()
                .map(value -> {
                    Map<String, AttributeValue> map = value.m();
                    String fileUrl = Optional.ofNullable(map.get(FILE_URL))
                            .map(AttributeValue::s)
                            .orElse("");
                    String fileName = Optional.ofNullable(map.get(FILE_NAME))
                            .map(AttributeValue::s)
                            .orElse("");
                    String fileSize = Optional.ofNullable(map.get(FILE_SIZE))
                            .map(AttributeValue::s)
                            .orElse("");
                    String sequence = Optional.ofNullable(map.get(SEQUENCE))
                            .map(AttributeValue::s)
                            .orElse("");
                    return new ApplyPortfolioDto(fileUrl, fileName, fileSize, sequence);
                })
                .collect(Collectors.toList());
    }

    @Override
    public EnhancedType<List<ApplyPortfolioDto>> type() {
        return EnhancedType.listOf(EnhancedType.of(ApplyPortfolioDto.class));
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.L;
    }
}
