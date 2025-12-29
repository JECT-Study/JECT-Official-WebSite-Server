package org.ject.support.external.dynamodb.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ject.support.external.dynamodb.util.CompositeKeyConverter;
import org.springframework.data.annotation.Id;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Deprecated
@Setter
@DynamoDbBean
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class EntityWithPrimaryKey implements Serializable {
    @Id
    protected CompositeKey pk;
    protected CompositeKey sk;

    @DynamoDbPartitionKey
    @DynamoDBHashKey(attributeName = "pk")
    @DynamoDbConvertedBy(value = CompositeKeyConverter.class)
    public CompositeKey getPk() {
        return pk;
    }

    @DynamoDbSortKey
    @DynamoDBRangeKey(attributeName = "sk")
    @DynamoDbConvertedBy(value = CompositeKeyConverter.class)
    public CompositeKey getSk() {
        return sk;
    }
}
