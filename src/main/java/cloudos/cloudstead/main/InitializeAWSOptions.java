package cloudos.cloudstead.main;

import com.amazonaws.auth.AWSCredentials;
import lombok.Getter;
import lombok.Setter;
import org.kohsuke.args4j.Option;

public class InitializeAWSOptions implements AWSCredentials {

    public static final String AWS_ACCESS_KEY = "AWS_ACCESS_KEY_ID";
    public static final String AWS_SECRET_KEY = "AWS_SECRET_ACCESS_KEY";
    @Getter @Setter private String aWSAccessKeyId = System.getenv().get(AWS_ACCESS_KEY);
    @Getter @Setter private String aWSSecretKey = System.getenv().get(AWS_SECRET_KEY);

    public static final String USAGE_BUCKET = "Name of the S3 bucket that cloudos admins will use. It will be created if it doesn't exist";
    public static final String OPT_BUCKET = "-b";
    public static final String LONGOPT_BUCKET = "--bucket";
    @Option(name=OPT_BUCKET, aliases=LONGOPT_BUCKET, usage=USAGE_BUCKET, required=true)
    @Getter @Setter private String bucket;

    public static final String USAGE_GROUP = "Name of the IAM group that cloudos admins will belong to. It will be created if it doesn't exist";
    public static final String OPT_GROUP = "-g";
    public static final String LONGOPT_GROUP = "--group";
    @Option(name=OPT_GROUP, aliases=LONGOPT_GROUP, usage=USAGE_GROUP, required=true)
    @Getter @Setter private String group;

    public static final String USAGE_POLICY = "Name of the IAM policy that will apply to the cloudos admins group.";
    public static final String OPT_POLICY = "-p";
    public static final String LONGOPT_POLICY = "--policy";
    @Option(name=OPT_POLICY, aliases=LONGOPT_POLICY, usage=USAGE_POLICY, required=false)
    @Getter @Setter private String policyName = "cloudos-admins-s3-folders-policy";

}
