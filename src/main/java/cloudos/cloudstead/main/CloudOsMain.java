package cloudos.cloudstead.main;

import cloudos.appstore.model.app.config.AppConfigurationMap;
import cloudos.cloudstead.service.cloudos.CloudOsStatus;
import org.cobbzilla.util.json.JsonUtil;
import org.cobbzilla.util.system.Sleep;
import org.cobbzilla.wizard.client.ApiClientBase;
import org.cobbzilla.wizard.util.RestResponse;

import static cloudos.cloudstead.resources.ApiConstants.CLOUDOS_ENDPOINT;
import static org.cobbzilla.util.json.JsonUtil.fromJson;
import static org.cobbzilla.util.json.JsonUtil.toJson;

public class CloudOsMain extends CloudsteadMainBase<CloudOsMainOptions> {

    @Override protected CloudOsMainOptions initOptions() { return new CloudOsMainOptions(); }

    public static void main (String[] args) throws Exception { main(CloudOsMain.class, args); }

    @Override protected void run() throws Exception {

        final ApiClientBase api = getApiClient();
        final CloudOsMainOptions options = getOptions();
        CloudOsStatus status;
        RestResponse response;

        final boolean hasName = options.hasName();
        if (!hasName && options.getOperation().requiresName()) {
            throw new UnsupportedOperationException("For operation "+options.getOperation()+", you must specify a name with "+CloudOsMainOptions.OPT_NAME+"/"+CloudOsMainOptions.LONGOPT_NAME);
        }
        final String name = options.getName();
        final String uri = CLOUDOS_ENDPOINT + "/" + name;

        switch (options.getOperation()) {
            case list:
                out(api.get(CLOUDOS_ENDPOINT).json);
                break;

            case status:
                out(api.get(uri+"/status").json);
                break;

            case view:
                if (hasName) {
                    out(api.get(uri).json);
                } else {
                    out(api.get(CLOUDOS_ENDPOINT).json);
                }
                break;

            case create:
                response = api.doPut(uri, toJson(options.getCloudOsRequest()));
                out(response.json);
                if (!response.isSuccess()) die("Error creating CloudOs, response status was "+response.status);
                break;

            case update:
                response = api.doPost(uri, toJson(options.getCloudOsRequest()));
                out(response.json);
                if (!response.isSuccess()) die("Error creating CloudOs, response status was "+response.status);
                break;

            case config:
                if (options.hasConfig()) {
                    try {
                        JsonUtil.fromJsonOrDie(options.getConfig(), AppConfigurationMap.class);
                    } catch (Exception e) {
                        die("Not a valid AppConfigurationMap ("+e.getMessage()+"): "+options.getConfig());
                    }
                    response = api.doPost(uri + "/config", options.getConfig());
                    out(response.json);
                    if (!response.isSuccess()) die("Error updating config, response status was "+response.status);

                } else {
                    out(api.get(uri + "/config").json);
                }
                break;

            case launch:
                status = fromJson(api.post(uri + "/launch", null).json, CloudOsStatus.class);
                while (!status.isCompleted()) {
                    out("awaiting completion, status="+toJson(status));
                    Sleep.sleep(5000);
                    status = fromJson(api.get(uri+"/status").json, CloudOsStatus.class);
                }
                out("completed: "+toJson(status));
                break;

            case destroy:
                out(api.delete(uri).toString());
                break;

            default:
                throw new UnsupportedOperationException("Unsupported operation: "+options.getOperation());
        }
    }
}
