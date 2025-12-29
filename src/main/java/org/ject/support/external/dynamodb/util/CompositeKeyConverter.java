package org.ject.support.external.dynamodb.util;

import org.ject.support.external.dynamodb.domain.CompositeKey;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Deprecated
public class CompositeKeyConverter implements AttributeConverter<CompositeKey> {
    @Override
    public AttributeValue transformFrom(final CompositeKey compositeKey) {
        return AttributeValue.fromS(compositeKey.toString());
    }

    @Override
    public CompositeKey transformTo(final AttributeValue attributeValue) {
        String[] parts = attributeValue.s().split(CompositeKey.DELIMITER);
        return new CompositeKey(parts[0], parts[1]);
    }

    @Override
    public EnhancedType<CompositeKey> type() {
        return EnhancedType.of(CompositeKey.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}
