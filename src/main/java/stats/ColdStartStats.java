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

public class ColdStartStats implements RequestStreamHandler {

    private static final String FUNCTION_NAME = System.getenv("STATS_FUNCTION_NAME");
    private static final String TABLE_NAME = System.getenv("DYNAMODB_TABLE");
    private static final Logger LOG = Logger.getLogger(ColdStartStats.class);
    private static final Regions REGION = Regions.US_EAST_1;

    private DynamoDB dynamoDb;
    private AWSLambdaClient lambdaClient = new AWSLambdaClient();


    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {

        try {
            LOG.info("Invoking cron");
            String payload = "{\"ref\":\"refs/heads/test-1\",\"before\":\"304251fa2bc8949169b718b11fe8ff5e64935e70\",\"after\":\"40876fbb1eef99652748e8e0ebd8f96c8cbd2286\",\"created\":false,\"deleted\":false,\"forced\":false,\"base_ref\":null,\"compare\":\"https://github.com/piczmar/git-code-stats/compare/304251fa2bc8...40876fbb1eef\",\"commits\":[{\"id\":\"40876fbb1eef99652748e8e0ebd8f96c8cbd2286\",\"tree_id\":\"005f0db86514a47522e135e8dc69291fe95b506c\",\"distinct\":true,\"message\":\"Update pom.xml\",\"timestamp\":\"2018-12-04T00:42:18+01:00\",\"url\":\"https://github.com/piczmar/git-code-stats/commit/40876fbb1eef99652748e8e0ebd8f96c8cbd2286\",\"author\":{\"name\":\"Marcin Piczkowski\",\"email\":\"piczmar@wp.pl\",\"username\":\"piczmar\"},\"committer\":{\"name\":\"GitHub\",\"email\":\"noreply@github.com\",\"username\":\"web-flow\"},\"added\":[],\"removed\":[],\"modified\":[\"pom.xml\"]}],\"head_commit\":{\"id\":\"40876fbb1eef99652748e8e0ebd8f96c8cbd2286\",\"tree_id\":\"005f0db86514a47522e135e8dc69291fe95b506c\",\"distinct\":true,\"message\":\"Update pom.xml\",\"timestamp\":\"2018-12-04T00:42:18+01:00\",\"url\":\"https://github.com/piczmar/git-code-stats/commit/40876fbb1eef99652748e8e0ebd8f96c8cbd2286\",\"author\":{\"name\":\"Marcin Piczkowski\",\"email\":\"piczmar@wp.pl\",\"username\":\"piczmar\"},\"committer\":{\"name\":\"GitHub\",\"email\":\"noreply@github.com\",\"username\":\"web-flow\"},\"added\":[],\"removed\":[],\"modified\":[\"pom.xml\"]},\"repository\":{\"id\":152924610,\"node_id\":\"MDEwOlJlcG9zaXRvcnkxNTI5MjQ2MTA=\",\"name\":\"git-code-stats\",\"full_name\":\"piczmar/git-code-stats\",\"private\":false,\"owner\":{\"name\":\"piczmar\",\"email\":\"piczmar@wp.pl\",\"login\":\"piczmar\",\"id\":497079,\"node_id\":\"MDQ6VXNlcjQ5NzA3OQ==\",\"avatar_url\":\"https://avatars2.githubusercontent.com/u/497079?v=4\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/piczmar\",\"html_url\":\"https://github.com/piczmar\",\"followers_url\":\"https://api.github.com/users/piczmar/followers\",\"following_url\":\"https://api.github.com/users/piczmar/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/piczmar/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/piczmar/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/piczmar/subscriptions\",\"organizations_url\":\"https://api.github.com/users/piczmar/orgs\",\"repos_url\":\"https://api.github.com/users/piczmar/repos\",\"events_url\":\"https://api.github.com/users/piczmar/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/piczmar/received_events\",\"type\":\"User\",\"site_admin\":false},\"html_url\":\"https://github.com/piczmar/git-code-stats\",\"description\":\"Project stats based on git history\",\"fork\":false,\"url\":\"https://github.com/piczmar/git-code-stats\",\"forks_url\":\"https://api.github.com/repos/piczmar/git-code-stats/forks\",\"keys_url\":\"https://api.github.com/repos/piczmar/git-code-stats/keys{/key_id}\",\"collaborators_url\":\"https://api.github.com/repos/piczmar/git-code-stats/collaborators{/collaborator}\",\"teams_url\":\"https://api.github.com/repos/piczmar/git-code-stats/teams\",\"hooks_url\":\"https://api.github.com/repos/piczmar/git-code-stats/hooks\",\"issue_events_url\":\"https://api.github.com/repos/piczmar/git-code-stats/issues/events{/number}\",\"events_url\":\"https://api.github.com/repos/piczmar/git-code-stats/events\",\"assignees_url\":\"https://api.github.com/repos/piczmar/git-code-stats/assignees{/user}\",\"branches_url\":\"https://api.github.com/repos/piczmar/git-code-stats/branches{/branch}\",\"tags_url\":\"https://api.github.com/repos/piczmar/git-code-stats/tags\",\"blobs_url\":\"https://api.github.com/repos/piczmar/git-code-stats/git/blobs{/sha}\",\"git_tags_url\":\"https://api.github.com/repos/piczmar/git-code-stats/git/tags{/sha}\",\"git_refs_url\":\"https://api.github.com/repos/piczmar/git-code-stats/git/refs{/sha}\",\"trees_url\":\"https://api.github.com/repos/piczmar/git-code-stats/git/trees{/sha}\",\"statuses_url\":\"https://api.github.com/repos/piczmar/git-code-stats/statuses/{sha}\",\"languages_url\":\"https://api.github.com/repos/piczmar/git-code-stats/languages\",\"stargazers_url\":\"https://api.github.com/repos/piczmar/git-code-stats/stargazers\",\"contributors_url\":\"https://api.github.com/repos/piczmar/git-code-stats/contributors\",\"subscribers_url\":\"https://api.github.com/repos/piczmar/git-code-stats/subscribers\",\"subscription_url\":\"https://api.github.com/repos/piczmar/git-code-stats/subscription\",\"commits_url\":\"https://api.github.com/repos/piczmar/git-code-stats/commits{/sha}\",\"git_commits_url\":\"https://api.github.com/repos/piczmar/git-code-stats/git/commits{/sha}\",\"comments_url\":\"https://api.github.com/repos/piczmar/git-code-stats/comments{/number}\",\"issue_comment_url\":\"https://api.github.com/repos/piczmar/git-code-stats/issues/comments{/number}\",\"contents_url\":\"https://api.github.com/repos/piczmar/git-code-stats/contents/{+path}\",\"compare_url\":\"https://api.github.com/repos/piczmar/git-code-stats/compare/{base}...{head}\",\"merges_url\":\"https://api.github.com/repos/piczmar/git-code-stats/merges\",\"archive_url\":\"https://api.github.com/repos/piczmar/git-code-stats/{archive_format}{/ref}\",\"downloads_url\":\"https://api.github.com/repos/piczmar/git-code-stats/downloads\",\"issues_url\":\"https://api.github.com/repos/piczmar/git-code-stats/issues{/number}\",\"pulls_url\":\"https://api.github.com/repos/piczmar/git-code-stats/pulls{/number}\",\"milestones_url\":\"https://api.github.com/repos/piczmar/git-code-stats/milestones{/number}\",\"notifications_url\":\"https://api.github.com/repos/piczmar/git-code-stats/notifications{?since,all,participating}\",\"labels_url\":\"https://api.github.com/repos/piczmar/git-code-stats/labels{/name}\",\"releases_url\":\"https://api.github.com/repos/piczmar/git-code-stats/releases{/id}\",\"deployments_url\":\"https://api.github.com/repos/piczmar/git-code-stats/deployments\",\"created_at\":1539475343,\"updated_at\":\"2018-10-14T16:23:49Z\",\"pushed_at\":1543880539,\"git_url\":\"git://github.com/piczmar/git-code-stats.git\",\"ssh_url\":\"git@github.com:piczmar/git-code-stats.git\",\"clone_url\":\"https://github.com/piczmar/git-code-stats.git\",\"svn_url\":\"https://github.com/piczmar/git-code-stats\",\"homepage\":null,\"size\":61,\"stargazers_count\":1,\"watchers_count\":1,\"language\":\"Java\",\"has_issues\":true,\"has_projects\":true,\"has_downloads\":true,\"has_wiki\":true,\"has_pages\":false,\"forks_count\":0,\"mirror_url\":null,\"archived\":false,\"open_issues_count\":3,\"license\":null,\"forks\":0,\"open_issues\":3,\"watchers\":1,\"default_branch\":\"master\",\"stargazers\":1,\"master_branch\":\"master\"},\"pusher\":{\"name\":\"piczmar\",\"email\":\"piczmar@wp.pl\"},\"sender\":{\"login\":\"piczmar\",\"id\":497079,\"node_id\":\"MDQ6VXNlcjQ5NzA3OQ==\",\"avatar_url\":\"https://avatars2.githubusercontent.com/u/497079?v=4\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/piczmar\",\"html_url\":\"https://github.com/piczmar\",\"followers_url\":\"https://api.github.com/users/piczmar/followers\",\"following_url\":\"https://api.github.com/users/piczmar/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/piczmar/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/piczmar/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/piczmar/subscriptions\",\"organizations_url\":\"https://api.github.com/users/piczmar/orgs\",\"repos_url\":\"https://api.github.com/users/piczmar/repos\",\"events_url\":\"https://api.github.com/users/piczmar/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/piczmar/received_events\",\"type\":\"User\",\"site_admin\":false}}";
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
