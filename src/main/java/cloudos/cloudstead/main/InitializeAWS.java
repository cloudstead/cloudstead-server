package cloudos.cloudstead.main;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.policy.Condition;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.auth.policy.resources.S3BucketResource;
import com.amazonaws.auth.policy.resources.S3ObjectResource;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.*;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import lombok.Getter;
import lombok.Setter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.util.List;

/**
 * Typically this is a one-time configuration step. It does the following:
 * <ul>
 *   <li>Creates the S3 bucket that hosted cloudOs configurations will be stored in</li>
 *   <li>Creates an IAM group that all cloudOs admins will be members of</li>
 *   <li>Sets permissions on the group such that, within the S3 bucket, IAM users have full control
 *   within a folder whose name is their username, and no permissions elsewhere</li>
 * </ul>
 *
 * When the API server is running, the name of this bucket and group are specified in the cloudConfig section.
 */
public class InitializeAWS {

    @Getter @Setter private String[] args;

    private InitializeAWSOptions options = new InitializeAWSOptions();
    private final CmdLineParser parser = new CmdLineParser(options);

    private AmazonIdentityManagementClient IAMclient;
    private AmazonS3Client S3client;

    public InitializeAWS(String[] args) { this.args = args; }

    public static void main (String[] args) throws Exception {
        InitializeAWS init = new InitializeAWS(args);
        init.run();
    }

    private void run() throws CmdLineException {

        parser.parseArgument(args);

        S3client = new AmazonS3Client(options, new ClientConfiguration().withProtocol(Protocol.HTTP));
        final List<Bucket> buckets = S3client.listBuckets();
        boolean bucketExists = false;
        for (Bucket b : buckets) {
            if (b.getName().equals(options.getBucket())) {
                bucketExists = true;
                break;
            }
        }
        if (!bucketExists) {
            S3client.createBucket(options.getBucket());
        }

        IAMclient = new AmazonIdentityManagementClient(options);
        try {
            IAMclient.getGroup(new GetGroupRequest(options.getGroup()));
        } catch (NoSuchEntityException e) {
            if (e.getStatusCode() == 404) {
                IAMclient.createGroup(new CreateGroupRequest(options.getGroup()));
            }
        }

        boolean policyExists = false;
        final ListGroupPoliciesResult listGroupPoliciesResult = IAMclient.listGroupPolicies(new ListGroupPoliciesRequest(options.getGroup()));
        for (String policyName : listGroupPoliciesResult.getPolicyNames()) {
            if (policyName.equals(options.getPolicyName())) {
                policyExists = true;
                break;
            }
        }

        if (!policyExists) {
            final Statement[] statements = {
                    new Statement(Statement.Effect.Allow)
                            .withId("AllowAllS3ActionsInUserFolder")
                            .withActions(S3Actions.AllS3Actions)
                            .withResources(new S3ObjectResource(options.getBucket(), "${aws:username}/*")),
                    new Statement(Statement.Effect.Allow)
                            .withId("AllowS3ListBucketInUserFolder")
                            .withActions(S3Actions.ListObjects)
                            .withResources(new S3BucketResource(options.getBucket()))
                            .withConditions(new Condition()
                                    .withConditionKey("s3:prefix")
                                    .withType("StringLike")
                                    .withValues("${aws:username}/*"))
            };

            IAMclient.putGroupPolicy(new PutGroupPolicyRequest()
                    .withGroupName(options.getGroup())
                    .withPolicyName(options.getPolicyName())
                    .withPolicyDocument(new Policy().withStatements(statements).toJson()));
        }
    }
}
