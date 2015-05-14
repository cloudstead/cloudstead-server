package cloudos.cloudstead.main;

import org.cobbzilla.wizard.client.ApiClientBase;
import org.cobbzilla.wizard.util.RestResponse;

import static cloudos.cloudstead.resources.ApiConstants.SEARCH_ENDPOINT;
import static org.cobbzilla.util.json.JsonUtil.toJson;

public class CloudsteadSearchMain extends CloudsteadMainBase<CloudsteadSearchOptions> {

    public static void main (String[] args) { main(CloudsteadSearchMain.class, args); }

    @Override protected void run() throws Exception {
        final CloudsteadSearchOptions options = getOptions();
        final ApiClientBase api = getApiClient();
        final String uri = SEARCH_ENDPOINT + "/" + options.getType();
        final RestResponse response = api.doPost(uri, toJson(options.getResultPage()));
        out(response.isSuccess() ? response.json : response.toString());
    }

}
