package org.ject.support.external.dynamodb.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.ject.support.domain.tempapply.domain.TemporaryApplication;
import org.ject.support.domain.tempapply.repository.TemporaryApplicationRepository;
import org.ject.support.external.dynamodb.domain.CompositeKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.ject.support.testconfig.IntegrationTest;

@IntegrationTest
class TemporaryApplicationRepositoryTest {
    @Autowired
    private TemporaryApplicationRepository temporaryApplicationRepository;


    @AfterEach
    void tearDown() {
        temporaryApplicationRepository.deleteAll();
    }

    @Order(1)
    @Test
    @DisplayName("dynamodb repository save test")
    void dynamodb_save() {
        // given
        TemporaryApplication temporaryApplication = new TemporaryApplication("1", Map.of("key", "value"));
        // when
        temporaryApplicationRepository.save(temporaryApplication);

        // then
        Optional<TemporaryApplication> optional = temporaryApplicationRepository.findByPartitionKeyAndSortKey(
                temporaryApplication.getPk(), temporaryApplication.getSk());
        assertThat(optional).isPresent();
        TemporaryApplication saved = optional.get();
        assertThat(saved).isEqualTo(temporaryApplication);
    }

    @Order(2)
    @Test
    @DisplayName("dynamodb repository find by partition key test")
    void find_by_partition_key() {
        // given
        temporaryApplicationRepository.save(new TemporaryApplication("1", Map.of("key", "value")));
        temporaryApplicationRepository.save(new TemporaryApplication("1", Map.of("key", "value")));
        temporaryApplicationRepository.save(new TemporaryApplication("1", Map.of("key", "value")));
        temporaryApplicationRepository.save(new TemporaryApplication("2", Map.of("key", "value")));
        temporaryApplicationRepository.save(new TemporaryApplication("2", Map.of("key", "value")));
        temporaryApplicationRepository.save(new TemporaryApplication("3", Map.of("key", "value")));
        temporaryApplicationRepository.save(new TemporaryApplication("4", Map.of("key", "value")));
        // when
        String prefix = "MEMBER";
        List<TemporaryApplication> members1 = temporaryApplicationRepository.findByPartitionKey(
                new CompositeKey(prefix, "1"));
        // then

        assertThat(members1).hasSize(3);
        assertThat(members1).allMatch(temporaryApplication -> temporaryApplication.getMemberId().equals("1"));
        assertThat(members1).isSortedAccordingTo(Comparator.comparing(TemporaryApplication::getTimestamp));

        List<TemporaryApplication> members2 = temporaryApplicationRepository.findByPartitionKey(
                new CompositeKey(prefix, "2"));
        assertThat(members2).hasSize(2);
        assertThat(members2).allMatch(temporaryApplication -> temporaryApplication.getMemberId().equals("2"));
        assertThat(members2).isSortedAccordingTo(Comparator.comparing(TemporaryApplication::getTimestamp));
    }

    @Order(3)
    @Test
    @DisplayName("dynamodb repository find by partition with sort type test")
    void find_by_partition_with_sort_type() {
        // given
        temporaryApplicationRepository.save(new TemporaryApplication("1", Map.of("key", "value")));
        temporaryApplicationRepository.save(new TemporaryApplication("1", Map.of("key", "value")));
        temporaryApplicationRepository.save(new TemporaryApplication("1", Map.of("key", "value")));
        temporaryApplicationRepository.save(new TemporaryApplication("2", Map.of("key", "value")));
        temporaryApplicationRepository.save(new TemporaryApplication("2", Map.of("key", "value")));
        temporaryApplicationRepository.save(new TemporaryApplication("3", Map.of("key", "value")));
        temporaryApplicationRepository.save(new TemporaryApplication("4", Map.of("key", "value")));
        // when
        String prefix = "TIMESTAMP";
        List<TemporaryApplication> members1 = temporaryApplicationRepository.findByPartitionWithSortType(
                new CompositeKey("MEMBER", "1"), prefix);
        // then
        assertThat(members1).hasSize(3);
        assertThat(members1).allMatch(temporaryApplication -> temporaryApplication.getMemberId().equals("1"));
        assertThat(members1).isSortedAccordingTo(Comparator.comparing(TemporaryApplication::getTimestamp));

        List<TemporaryApplication> members2 = temporaryApplicationRepository.findByPartitionWithSortType(
                new CompositeKey("MEMBER", "2"), prefix);
        assertThat(members2).hasSize(2);
        assertThat(members2).allMatch(temporaryApplication -> temporaryApplication.getMemberId().equals("2"));
        assertThat(members2).isSortedAccordingTo(Comparator.comparing(TemporaryApplication::getTimestamp));
    }

}
