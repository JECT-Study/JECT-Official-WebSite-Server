package org.ject.support.external.dynamodb.repository;

import org.ject.support.external.dynamodb.domain.CompositeKey;
import org.ject.support.external.dynamodb.domain.EntityWithPrimaryKey;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DynamoDbRepository<T extends EntityWithPrimaryKey> {
    T save(T entity);

    List<T> findByPartitionKey(CompositeKey partitionKey);

    Optional<T> findByPartitionKeyAndSortKey(CompositeKey partitionKey, CompositeKey sortKey);

    List<T> findByPartitionWithSortType(CompositeKey partitionKey, String sortPrefix);

    void delete(T entity);

    void deleteAll();

    void deleteByPartitionKey(CompositeKey partitionKey);
}
