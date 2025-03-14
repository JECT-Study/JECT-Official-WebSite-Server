package org.ject.support.domain.tempapply.repository;

import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import java.util.Comparator;
import java.util.Optional;
import org.ject.support.domain.tempapply.domain.TemporaryApplication;
import org.ject.support.external.dynamodb.domain.CompositeKey;
import org.ject.support.external.dynamodb.repository.AbstractDynamoDbRepository;
import org.springframework.stereotype.Repository;

@Repository
public class TemporaryApplicationRepository extends AbstractDynamoDbRepository<TemporaryApplication> {
    public TemporaryApplicationRepository(final DynamoDbTemplate dynamoDbTemplate) {
        super(dynamoDbTemplate);
    }

    @Override
    protected void setEntityClass() {
        entityClass = TemporaryApplication.class;
    }

    //TODO 2025 02 21 10:08:04 : 최신 문서임을 꼭 application level에서 확인해야 하는가?
    public Optional<TemporaryApplication> findLatestByMemberId(String memberId) {
        CompositeKey partitionKey = new CompositeKey(TemporaryApplication.PK_PREFIX, memberId);
        return findByPartitionWithSortType(partitionKey, TemporaryApplication.SK_PREFIX)
                .stream().max(Comparator.comparing(TemporaryApplication::getTimestamp));
    }

    public void deleteByMemberId(String memberId) {
        CompositeKey partitionKey = new CompositeKey(TemporaryApplication.PK_PREFIX, memberId);
        deleteByPartitionKey(partitionKey);
    }
}
