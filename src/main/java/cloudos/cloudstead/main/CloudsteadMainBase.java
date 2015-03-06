package cloudos.cloudstead.main;

import cloudos.cloudstead.model.auth.CloudsteadAuthResponse;
import cloudos.cloudstead.resources.ApiConstants;
import cloudos.model.auth.LoginRequest;
import org.cobbzilla.wizard.main.MainApiBase;
import org.cobbzilla.wizard.util.RestResponse;

import static org.cobbzilla.util.json.JsonUtil.fromJson;

public abstract class CloudsteadMainBase<OPT extends CloudsteadMainOptions> extends MainApiBase<OPT> {

    @Override protected String getApiHeaderTokenName() { return ApiConstants.H_API_KEY; }

    @Override protected Object buildLoginRequest(OPT options) {
        return new LoginRequest().setName(options.getAccount()).setPassword(options.getPassword());
    }

    @Override protected String getLoginUri() { return ApiConstants.ADMINS_ENDPOINT; }

    @Override protected String getSessionId(RestResponse response) throws Exception {
        CloudsteadAuthResponse authResponse = fromJson(response.json, CloudsteadAuthResponse.class);
        return authResponse.getSessionId();
    }

    @Override protected void setSecondFactor(Object loginRequest, String token) {
        ((LoginRequest) loginRequest).setSecondFactor(token);
    }
}
