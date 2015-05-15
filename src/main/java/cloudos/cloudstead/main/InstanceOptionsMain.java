package cloudos.cloudstead.main;

import cloudos.cloudstead.resources.ApiConstants;
import org.cobbzilla.wizard.util.RestResponse;

public class InstanceOptionsMain extends CloudsteadMainBase<InstanceOptionsMainOptions> {

    public static void main (String[] args) { main(InstanceOptionsMain.class, args); }

    @Override protected void run() throws Exception {
        final String url = ApiConstants.INSTANCE_OPTIONS_ENDPOINT + "/" + getOptions().getOption().name();
        final RestResponse response = getApiClient().get(url);
        out(response.json);
    }

}
