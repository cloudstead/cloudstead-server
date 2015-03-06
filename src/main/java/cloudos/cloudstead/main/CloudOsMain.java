package cloudos.cloudstead.main;

import cloudos.cloudstead.model.support.CloudOsRequest;
import cloudos.cloudstead.service.cloudos.CloudOsStatus;
import org.cobbzilla.util.system.Sleep;
import org.cobbzilla.wizard.api.CrudOperation;
import org.cobbzilla.wizard.client.ApiClientBase;

import static cloudos.cloudstead.resources.ApiConstants.CLOUDOS_ENDPOINT;
import static org.cobbzilla.util.json.JsonUtil.fromJson;
import static org.cobbzilla.util.json.JsonUtil.toJson;

public class CloudOsMain extends CloudsteadMainBase<CloudOsMainOptions> {

    @Override protected CloudOsMainOptions initOptions() { return new CloudOsMainOptions(); }

    public static void main (String[] args) throws Exception { main(CloudOsMain.class, args); }

    @Override protected void run() throws Exception {

        final ApiClientBase api = getApiClient();
        final CloudOsMainOptions options = getOptions();

        final boolean hasName = options.hasName();
        if (!hasName && options.getOperation() != CrudOperation.read) {
            throw new UnsupportedOperationException("Must specify a name with "+CloudOsMainOptions.OPT_NAME+"/"+CloudOsMainOptions.LONGOPT_NAME);
        }
        final String name = options.getName();
        final String uri = CLOUDOS_ENDPOINT + "/" + name;

        switch (options.getOperation()) {
            case read:
                if (hasName) {
                    out(api.get(uri).json);
                    out(api.get(uri + "/status").json);
                } else {
                    out(api.get(CLOUDOS_ENDPOINT).json);
                }
                break;

            case create:
                final CloudOsRequest request = new CloudOsRequest()
                        .setName(name)
                        .setEdition(options.getEdition())
                        .setRegion(options.getRegion())
                        .setAppBundle(options.getAppBundle())
                        .setAdditionalApps(options.getAdditionalApps());
                CloudOsStatus status = fromJson(api.put(uri, toJson(request)).json, CloudOsStatus.class);

                while (!status.isCompleted()) {
                    out("awaiting completion, status="+toJson(status));
                    Sleep.sleep(5000);
                    status = fromJson(api.get(uri+"/status").json, CloudOsStatus.class);
                }
                out("completed: "+toJson(status));
                break;

            case update:
                throw new UnsupportedOperationException("Cannot update a CloudOs");

            case delete:
                out(api.delete(uri).toString());
                break;

            default:
                throw new UnsupportedOperationException("Unsupported operation: "+options.getOperation());
        }
    }
}
