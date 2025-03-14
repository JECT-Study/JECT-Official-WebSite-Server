package org.ject.support.external.dynamodb.util;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ListMapConverter implements AttributeConverter<List<Map<String, String>>> {

    @Override
    public AttributeValue transformFrom(List<Map<String, String>> list) {
        return AttributeValue.builder()
                .l(list.stream()
                        .map(map -> AttributeValue.builder()
                                .m(map.entrySet().stream()
                                        .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                e -> AttributeValue.builder().s(e.getValue()).build())))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public List<Map<String, String>> transformTo(AttributeValue attributeValue) {
        return attributeValue.l().stream()
                .map(map -> map.m().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, e -> e.getValue().s()
                        )))
                .collect(Collectors.toList());
    }

    @Override
    public EnhancedType<List<Map<String, String>>> type() {
        return EnhancedType.listOf(EnhancedType.mapOf(String.class, String.class));
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.L;
    }
}
