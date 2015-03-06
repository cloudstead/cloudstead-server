package cloudos.cloudstead.main;

import org.cobbzilla.wizard.main.MainApiOptionsBase;

public class CloudsteadMainOptions extends MainApiOptionsBase {

    @Override protected String getDefaultApiBaseUri() { return "http://127.0.0.1:4001/api"; }

    @Override protected String getPasswordEnvVarName() { return "CSTEAD_PASS"; }

}
