package org.ject.support.external.dynamodb.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.ject.support.external.dynamodb.repository.DynamoDbRepository;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Deprecated
@Configuration
@DependsOn(value = {"awsConfig"})
@EnableDynamoDBRepositories(basePackageClasses = DynamoDbRepository.class)
public class DynamoDbConfig {

    @Value("${aws.dynamodb.endpoint}")
    private String dynamoDbEndpoint;

    @Bean
    public DynamoDbClient dynamoDbClient(AwsCredentials awsCredentials, Region region) {
        return DynamoDbClient.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .endpointOverride(URI.create(dynamoDbEndpoint))
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Bean
    public DynamoDbTemplate dynamoDbTemplate(DynamoDbEnhancedClient dynamoDbClient) {
        return new DynamoDbTemplate(dynamoDbClient);
    }

    @Bean
    public AmazonDynamoDB amazonDynamoDB(AwsCredentials awsCredentials, Region region) {
        return AmazonDynamoDBClient.builder()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(dynamoDbEndpoint, region.id())
                )
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(
                                        awsCredentials.accessKeyId(),
                                        awsCredentials.secretAccessKey()
                                )
                        )
                )
                .withClientConfiguration(new ClientConfiguration())
                .build();
    }
}
