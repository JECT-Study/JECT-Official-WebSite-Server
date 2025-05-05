package org.ject.support.domain.tempapply.repository;

import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.ject.support.domain.tempapply.domain.TemporaryApplication;
import org.ject.support.external.dynamodb.domain.CompositeKey;
import org.ject.support.external.dynamodb.repository.AbstractDynamoDbRepository;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

    public List<String> findMemberIdsByJobFamilyAndAfter(String jobFamily, LocalDateTime recruitStartDateTime) {
        ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder().build();
        return dynamoDbTemplate.scan(scanEnhancedRequest, entityClass)
                .items().stream()
                .filter(item -> item.getTimestamp().isAfter(recruitStartDateTime))
                .filter(item -> item.getJobFamily().equals(jobFamily))
                .map(TemporaryApplication::getMemberId)
                .distinct()
                .toList();
    }

    public void deleteByMemberId(String memberId) {
        CompositeKey partitionKey = new CompositeKey(TemporaryApplication.PK_PREFIX, memberId);
        deleteByPartitionKey(partitionKey);
    }
}
