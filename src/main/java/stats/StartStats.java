package stats;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

public class StartStats implements RequestStreamHandler {

    private static final String FUNCTION_NAME = System.getenv("STATS_FUNCTION_NAME");
    private static final String TABLE_NAME = System.getenv("DYNAMODB_TABLE");
    private static final Logger LOG = Logger.getLogger(StartStats.class);
    private static final Regions REGION = Regions.US_EAST_1;

    private DynamoDB dynamoDb;
    private AWSLambdaClient lambdaClient = new AWSLambdaClient();

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {

        try {
            LOG.info("Invoking cron");
            String payload = "";
            this.initDynamoDbClient();

            InvokeRequest request = new InvokeRequest()
                .withFunctionName(FUNCTION_NAME)
                .withInvocationType(InvocationType.Event)
                .withPayload(payload);

            long start = System.currentTimeMillis();
            lambdaClient.invoke(request);
            long delta = System.currentTimeMillis() - start;

            persistData(new StatsRecord(System.currentTimeMillis(), delta));
            LOG.info("Invoked function");

        } catch (Exception e) {
            LOG.error("Failed to invoke processing function", e);
            outputStream.write("Error".getBytes());
        }
        outputStream.write("OK".getBytes());
    }

    private void persistData(StatsRecord record)
        throws ConditionalCheckFailedException {
        this.dynamoDb.getTable(TABLE_NAME)
            .putItem(
                new PutItemSpec().withItem(new Item()
                    .withLong("timestamp", record.getTimestamp())
                    .withLong("duration", record.getDuration())));
    }

    private void initDynamoDbClient() {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(REGION));
        this.dynamoDb = new DynamoDB(client);
    }
}
