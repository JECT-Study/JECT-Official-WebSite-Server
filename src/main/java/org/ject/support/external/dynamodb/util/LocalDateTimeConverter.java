package org.ject.support.external.dynamodb.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Deprecated
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public AttributeValue transformFrom(final LocalDateTime localDateTime) {
        return AttributeValue.fromS(localDateTime.format(FORMATTER));
    }

    @Override
    public LocalDateTime transformTo(final AttributeValue attributeValue) {
        return LocalDateTime.parse(attributeValue.s());
    }

    @Override
    public EnhancedType<LocalDateTime> type() {
        return EnhancedType.of(LocalDateTime.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}
