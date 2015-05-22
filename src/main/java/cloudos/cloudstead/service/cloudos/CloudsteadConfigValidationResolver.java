package cloudos.cloudstead.service.cloudos;

import cloudos.appstore.model.app.config.AppConfigValidationResolver;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

/**
 * This is the resolver used by AppConfiguration.validate before a cloudstead is launched.
 * As such, the only groups available are the default groups, and no accounts are available (they haven't been created).
 */
public class CloudsteadConfigValidationResolver implements AppConfigValidationResolver {

    public static final CloudsteadConfigValidationResolver INSTANCE = new CloudsteadConfigValidationResolver();

    @Override public boolean isValidGroup(String name) {
        return !empty(name) && (name.equals("cloudos-users") || name.equals("cloudos-admins"));
    }

    // No accounts exist
    @Override public boolean isValidAccount(String name) { return false; }

}
