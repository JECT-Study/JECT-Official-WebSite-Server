package org.ject.support.external.dynamodb.util;

import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Deprecated
public class MapConverter implements AttributeConverter<Map<String, String>> {
    @Override
    public AttributeValue transformFrom(final Map<String, String> map) {
        return AttributeValue.builder()
                .m(map.entrySet().stream()
                        .collect(Collectors
                                .toMap(Map.Entry::getKey, e ->
                                        AttributeValue.builder()
                                                .s(e.getValue())
                                                .build())))
                .build();
    }

    @Override
    public Map<String, String> transformTo(final AttributeValue attributeValue) {
        return attributeValue.m().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().s()));
    }

    @Override
    public EnhancedType<Map<String, String>> type() {
        return EnhancedType.mapOf(String.class, String.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.M;
    }
}
