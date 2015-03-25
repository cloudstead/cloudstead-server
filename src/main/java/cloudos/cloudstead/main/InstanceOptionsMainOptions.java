package cloudos.cloudstead.main;

import cloudos.cloudstead.model.support.CloudOsInstanceOption;
import lombok.Getter;
import lombok.Setter;
import org.kohsuke.args4j.Option;

public class InstanceOptionsMainOptions extends CloudsteadMainOptions {

    public static final String USAGE_OPTION = "List all available choices for this option";
    public static final String OPT_OPTION = "-o";
    public static final String LONGOPT_OPTION = "--option";
    @Option(name=OPT_OPTION, aliases=LONGOPT_OPTION, usage=USAGE_OPTION, required=true)
    @Getter @Setter private CloudOsInstanceOption option;

}
