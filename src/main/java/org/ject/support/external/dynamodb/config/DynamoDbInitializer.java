package org.ject.support.external.dynamodb.config;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

@Slf4j
@Profile("!prod")
@Component
@RequiredArgsConstructor
public class DynamoDbInitializer implements ApplicationRunner, DisposableBean {
    private static boolean started = false;
    private static final String TABLE_NAME = "temporary_application";
    private static final String PK = "pk";
    private static final String SK = "sk";
    private static DynamoDBProxyServer server;
    private final DynamoDbClient ddbClient;


    @Override
    public void run(final ApplicationArguments args) throws Exception {
        if (started) {
            return;
        }

        final String[] localArgs = {"-inMemory"};
        server = ServerRunner.createServerFromCommandLineArgs(localArgs);
        server.start();
        started = true;

        if (ddbClient.listTables().tableNames().stream().anyMatch(tableName -> tableName.equals(TABLE_NAME))) {
            return;
        }

        CreateTableResponse response = generateCreateTableRequest();
        log.info("Table created: {}", response.tableDescription().tableName());
    }

    @Override
    public void destroy() throws Exception {
        if (server != null) {
            server.stop();
        }
        started = false;
        log.info("Stopping DynamoDB Local");
    }


    private CreateTableResponse generateCreateTableRequest() {
        return ddbClient.createTable(
                builder -> builder.tableName(TABLE_NAME)
                        .keySchema(
                                KeySchemaElement.builder()
                                        .attributeName(PK)
                                        .keyType(KeyType.HASH)
                                        .build(),
                                KeySchemaElement.builder()
                                        .attributeName(SK)
                                        .keyType(KeyType.RANGE)
                                        .build()
                        ).attributeDefinitions(
                                AttributeDefinition.builder()
                                        .attributeName(PK)
                                        .attributeType(ScalarAttributeType.S)
                                        .build(),
                                AttributeDefinition.builder()
                                        .attributeName(SK)
                                        .attributeType(ScalarAttributeType.S)
                                        .build()
                        ).provisionedThroughput(
                                ProvisionedThroughput.builder()
                                        .readCapacityUnits(5L)
                                        .writeCapacityUnits(5L)
                                        .build()
                        )
        );
    }
}
