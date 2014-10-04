package cloudos.cloudstead.model.auth;

import cloudos.cloudstead.model.Admin;
import cloudos.model.auth.AuthResponse;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CloudsteadAuthResponse extends AuthResponse<Admin> {

    public static final CloudsteadAuthResponse TWO_FACTOR = new CloudsteadAuthResponse(true);

    private CloudsteadAuthResponse(boolean twoFactor) { setSessionId(TWO_FACTOR_SID); }

    public CloudsteadAuthResponse(String sessionId, Admin admin) { super(sessionId, admin); }

}
