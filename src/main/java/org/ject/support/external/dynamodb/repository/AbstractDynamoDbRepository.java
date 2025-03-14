package org.ject.support.external.dynamodb.repository;

import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.ject.support.external.dynamodb.domain.CompositeKey;
import org.ject.support.external.dynamodb.domain.EntityWithPrimaryKey;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public abstract class AbstractDynamoDbRepository<T extends EntityWithPrimaryKey> implements DynamoDbRepository<T> {
    protected final DynamoDbTemplate dynamoDbTemplate;
    protected Class<T> entityClass;

    @PostConstruct
    protected abstract void setEntityClass();

    @Override
    public T save(final T entity) {
        return dynamoDbTemplate.save(entity);
    }

    @Override
    public List<T> findByPartitionKey(final CompositeKey partitionKey) {
        QueryConditional condition = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(partitionKey.toString()).build()
        );
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(condition)
                .build();
        return dynamoDbTemplate.query(request, entityClass)
                .items()
                .stream().toList();
    }

    @Override
    public Optional<T> findByPartitionKeyAndSortKey(final CompositeKey partitionKey, final CompositeKey sortKey) {
        Key key = Key.builder().partitionValue(partitionKey.toString()).sortValue(sortKey.toString()).build();
        T result = dynamoDbTemplate.load(key, entityClass);
        return Optional.ofNullable(result);
    }

    @Override
    public List<T> findByPartitionWithSortType(final CompositeKey partitionKey, final String sortPrefix) {
        Key key = Key.builder()
                .partitionValue(partitionKey.toString())
                .sortValue(sortPrefix)
                .build();
        QueryConditional condition = QueryConditional.sortBeginsWith(key);
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(condition)
                .build();

        return dynamoDbTemplate.query(request, entityClass)
                .items()
                .stream().toList();
    }

    @Override
    public void delete(final T entity) {
        dynamoDbTemplate.delete(entity);
    }

    @Override
    public void deleteAll() {
        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .build();

        dynamoDbTemplate.scan(request, entityClass)
                .items().stream().forEach(dynamoDbTemplate::delete);
    }

    @Override
    public void deleteByPartitionKey(final CompositeKey partitionKey) {
        Key key = Key.builder()
                .partitionValue(partitionKey.toString())
                .build();
        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key))
                .build();
        dynamoDbTemplate.query(queryEnhancedRequest, entityClass)
                .items()
                .forEach(dynamoDbTemplate::delete);
    }
}
