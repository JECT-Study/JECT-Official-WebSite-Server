package org.ject.support.domain.tempapply.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.external.dynamodb.domain.CompositeKey;
import org.ject.support.external.dynamodb.domain.EntityWithPrimaryKey;
import org.ject.support.external.dynamodb.util.ListMapConverter;
import org.ject.support.external.dynamodb.util.LocalDateTimeConverter;
import org.ject.support.external.dynamodb.util.MapConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;

@DynamoDBTable(tableName = "temporary_application")
@DynamoDbBean
@NoArgsConstructor
@Setter
@EqualsAndHashCode(callSuper = false)
public class TemporaryApplication extends EntityWithPrimaryKey {
    public static final String PK_PREFIX = "MEMBER";
    public static final String SK_PREFIX = "TIMESTAMP";

    private String memberId;
    private LocalDateTime timestamp;
    private String jobFamily;
    private Map<String, String> answers;
    private List<Map<String, String>> portfolios;

    public TemporaryApplication(final String memberId,
                                final Map<String, String> answers,
                                final String jobFamily,
                                final List<Map<String, String>> portfolios) {
        this.memberId = memberId;
        this.timestamp = LocalDateTime.now();
        this.answers = answers;
        this.jobFamily = jobFamily;
        this.portfolios = portfolios;
        this.pk = new CompositeKey(PK_PREFIX, this.memberId);
        this.sk = new CompositeKey(SK_PREFIX, this.timestamp.toString());
    }

    @DynamoDbAttribute(value = "member_id")
    public String getMemberId() {
        return memberId;
    }

    @DynamoDbAttribute(value = "timestamp")
    @DynamoDbConvertedBy(value = LocalDateTimeConverter.class)
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @DynamoDbAttribute(value = "job_family")
    public String getJobFamily() {
        return jobFamily;
    }

    @DynamoDbAttribute(value = "answers")
    @DynamoDbConvertedBy(value = MapConverter.class)
    public Map<String, String> getAnswers() {
        return answers;
    }

    @DynamoDbAttribute(value = "portfolios")
    @DynamoDbConvertedBy(value = ListMapConverter.class)
    public List<Map<String, String>> getPortfolios() {
        return portfolios;
    }

    public boolean isSameJobFamily(JobFamily jobFamily) {
        return this.jobFamily.equals(jobFamily.name());
    }
}
