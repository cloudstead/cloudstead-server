package cloudos.cloudstead.main;

import org.cobbzilla.wizard.client.ApiClientBase;
import org.cobbzilla.wizard.util.RestResponse;

import static cloudos.cloudstead.resources.ApiConstants.ADMINS_ENDPOINT;
import static org.cobbzilla.util.json.JsonUtil.toJson;
import static org.cobbzilla.util.string.StringUtil.urlEncode;

public class CloudsteadAdminMain extends CloudsteadMainBase<CloudsteadAdminMainOptions> {

    public static void main (String[] args) { main(CloudsteadAdminMain.class, args); }

    @Override protected void run() throws Exception {
        final CloudsteadAdminMainOptions options = getOptions();
        final ApiClientBase api = getApiClient();
        final String name = options.hasName() ? options.getName() : options.getAccount();
        final String uri = ADMINS_ENDPOINT + "/" + urlEncode(name);
        final RestResponse response;

        switch (options.getOperation()) {
            case create:
                response = api.doPut(uri, toJson(options.getAdminRequest()));
                out(response.isSuccess() ? response.json : response.toString());
                break;

            case read:
                response = api.doGet(uri);
                out(response.isSuccess() ? response.json : response.toString());
                break;

            case update:
                response = api.doPost(uri, toJson(options.getAdminRequest()));
                out(response.isSuccess() ? response.json : response.toString());
                break;

            case delete:
                response = api.doDelete(uri);
                out(response.toString());
                break;

            default:
                die("invalid operation: " + options.getOperation());
                break;
        }
    }
}
