package cloudos.cloudstead.resources;

import cloudos.appstore.model.app.AppLevel;
import cloudos.appstore.model.app.AppManifest;
import cloudos.appstore.model.app.config.AppConfiguration;
import cloudos.appstore.model.app.config.AppConfigurationMap;
import cloudos.cloudstead.dao.AdminDAO;
import cloudos.cloudstead.dao.CloudOsDAO;
import cloudos.cloudstead.dao.CloudOsEventDAO;
import cloudos.cloudstead.dao.SessionDAO;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.model.CloudOsEvent;
import cloudos.cloudstead.model.support.CloudOsRequest;
import cloudos.cloudstead.model.support.CloudOsState;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.cloudstead.service.cloudos.CloudOsLaunchManager;
import cloudos.cloudstead.service.cloudos.CloudOsStatus;
import cloudos.cloudstead.service.cloudos.CloudsteadConfigValidationResolver;
import com.qmino.miredot.annotations.ReturnType;
import edu.emory.mathcs.backport.java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.cobbzilla.util.io.Tarball;
import org.cobbzilla.util.system.CommandShell;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;
import org.cobbzilla.wizard.validation.SimpleViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rooty.toots.chef.ChefSolo;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.http.HttpStatusCodes.UNPROCESSABLE_ENTITY;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.listFiles;
import static org.cobbzilla.util.system.CommandShell.chmod;
import static org.cobbzilla.wizard.resources.ResourceUtil.*;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path(ApiConstants.CLOUDOS_ENDPOINT)
@Service @Slf4j
public class CloudOsResource {

    public static final CloudsteadConfigValidationResolver RESOLVER = CloudsteadConfigValidationResolver.INSTANCE;
    public static final String[] LAUNCHTIME_APPS = {"cloudos", "email"};

    public static Map<String, List<ConstraintViolationBean>> validateConfig(AppConfigurationMap configMap) {
        return configMap.validate(RESOLVER, LAUNCHTIME_APPS);
    }

    public Response invalidConfig(AppConfigurationMap configMap) { return status(UNPROCESSABLE_ENTITY, configMap); }

    @Autowired private CloudOsDAO cloudOsDAO;
    @Autowired private SessionDAO sessionDAO;
    @Autowired private AdminDAO adminDAO;
    @Autowired private CloudOsLaunchManager launchManager;
    @Autowired private CloudOsEventDAO eventDAO;
    @Autowired private CloudsteadConfiguration configuration;

    /**
     * List your CloudOs instances
     * @param apiKey The session ID
     * @return A List of your CloudOs instances
     */
    @GET
    @ReturnType("java.util.List<cloudos.cloudstead.model.CloudOs>")
    public Response findAll (@HeaderParam(ApiConstants.H_API_KEY) String apiKey) {

        final CloudOsContext ctx = new CloudOsContext(apiKey, null);
        if (ctx.response != null) return ctx.response;

        final List<CloudOs> found = cloudOsDAO.findByAdmin(ctx.admin.getUuid());
        return ok(found);
    }

    /**
     * Find a single CloudOs instance
     * @param apiKey The session ID
     * @param name The name of the instance
     * @return A CloudOs instance
     * @statuscode 403 instance is owned by another user
     * @statuscode 404 no instance with that name
     */
    @GET
    @Path("/{name}")
    @ReturnType("cloudos.cloudstead.model.CloudOs")
    public Response find (@HeaderParam(ApiConstants.H_API_KEY) String apiKey,
                          @PathParam("name") String name) {

        final CloudOsContext ctx = new CloudOsContext(apiKey, name);
        if (ctx.response != null) return ctx.response;

        return ok(ctx.cloudOs);
    }

    /**
     * Create (but do not yet launch) a new cloudstead
     * @param apiKey The session ID
     * @param name The name of the instance
     * @param request The CloudOs request
     * @return The new CloudOs instance
     */
    @PUT
    @Path("/{name}")
    @ReturnType("cloudos.cloudstead.model.CloudOs")
    public Response create (@HeaderParam(ApiConstants.H_API_KEY) String apiKey,
                            @PathParam("name") String name,
                            @Valid CloudOsRequest request) {
        final CloudOsContext ctx = new CloudOsContext(apiKey, null);
        if (ctx.response != null) return ctx.response;

        // sanity check
        if (!name.equalsIgnoreCase(request.getName())) return invalid();

        // email must be verified
        ctx.admin = adminDAO.findByUuid(ctx.admin.getUuid());
        if (!ctx.admin.isEmailVerified()) return invalid("{err.cloudos.create.unverifiedEmail}");

        // non-admins: cannot have more than max # of active cloudsteads
        if (!ctx.admin.isAdmin() && cloudOsDAO.findActiveByAdmin(ctx.admin.getUuid()).size() >= ctx.admin.getMaxCloudsteads()) {
            return invalid("{err.cloudos.create.maxCloudsteads}");
        }

        // region names vary across cloud vendors. validate here
        if (!request.getRegion().isValid()) return invalid("{err.cloudos.create.region.invalid}");

        ctx.cloudOs = new CloudOs();
        ctx.cloudOs.populate(ctx.admin, request);

        // check for duplicate
        if (cloudOsDAO.findByName(ctx.cloudOs.getName()) != null) return invalid("{err.cloudos.create.name.notUnique}");

        // pull apps from app store, set up dir + files to launch instance
        if (!prepChefStagingDir(ctx.cloudOs)) return serverError();

        // save instance to DB and return OK
        return ok(cloudOsDAO.create(ctx.cloudOs));
    }

    /**
     * Update a CloudOs (before it is launched)
     * @param apiKey The session ID
     * @param name The name of the instance
     * @param request The CloudOs request
     * @return The updated CloudOs instance
     */
    @POST
    @Path("/{name}")
    @ReturnType("cloudos.cloudstead.model.CloudOs")
    public Response update (@HeaderParam(ApiConstants.H_API_KEY) String apiKey,
                            @PathParam("name") String name,
                            CloudOsRequest request) {
        final CloudOsContext ctx = new CloudOsContext(apiKey, name);
        if (ctx.response != null) return ctx.response;

        if (ctx.cloudOs.getState() != CloudOsState.initial) return invalid("{err.cloudos.update.notInitial}");

        // determine if the list of apps has changed
        final List<String> requestApps = request.getAllApps();
        final List<String> currentApps = ctx.cloudOs.getAllApps();
        final boolean sameApps = requestApps.containsAll(currentApps) && currentApps.containsAll(requestApps);

        ctx.cloudOs.populate(ctx.admin, request);
        ctx.cloudOs = cloudOsDAO.update(ctx.cloudOs);

        // app list has changed, update chef staging directory
        if (!sameApps && !prepChefStagingDir(ctx.cloudOs)) return serverError();

        return ok(ctx.cloudOs);
    }

    /**
     * Retrieve initial configuration for a cloudstead. Once started, this doesn't matter too much.
     * @param apiKey The session ID
     * @param name The name of the instance
     * @return The AppConfigurationMap, containing configs for all apps to be installed
     */
    @GET
    @Path("/{name}/config")
    @ReturnType("cloudos.appstore.model.app.config.AppConfigurationMap")
    public Response getConfig (@HeaderParam(ApiConstants.H_API_KEY) String apiKey,
                               @PathParam("name") String name) {
        final CloudOsContext ctx = new CloudOsContext(apiKey, name);
        if (ctx.response != null) return ctx.response;

        if (ctx.cloudOs.getState() != CloudOsState.initial) return invalid("{err.cloudos.getConfig.notInitial}");

        final String locale = ctx.admin.getLocale();
        final AppConfigurationMap configMap = getAppConfiguration(ctx.cloudOs, locale);

        // Do not show end-user config for launch-time apps
        for (String app : LAUNCHTIME_APPS) configMap.removeConfig(app);

        // Validate, so the end-user knows what config still need to be filled out
        validateConfig(configMap);

        return ok(configMap);
    }

    /**
     * Update the initial configuration for a cloudstead.
     * @param apiKey The session ID
     * @param name The name of the instance
     * @param configMap The configuration updates. Apps that are not present in this map will not be updated.
     * @return The AppConfigurationMap, containing configs for all apps to be installed.
     * @statuscode 200 If the configuration was successfully written
     * @statuscode 422 If the configuration had validation errors. Check the "violations" attribute of the response JSON, it will be a Map of AppName->ConstraintViolationBean[]
     */
    @POST
    @Path("/{name}/config")
    @ReturnType("cloudos.appstore.model.app.config.AppConfigurationMap")
    public Response setConfig (@HeaderParam(ApiConstants.H_API_KEY) String apiKey,
                               @PathParam("name") String name,
                               AppConfigurationMap configMap) {
        final CloudOsContext ctx = new CloudOsContext(apiKey, name);
        if (ctx.response != null) return ctx.response;

        if (ctx.cloudOs.getState() != CloudOsState.initial) return invalid("{err.cloudos.setConfig.notInitial}");

        // Sanity check -- ensure we never allow end-user to set these (for now)
        for (String app : LAUNCHTIME_APPS) {
            if (configMap.getAppConfigs().containsKey(app)) {
                log.warn("Cannot set config for launch-time app (omitting): "+app);
                configMap.removeConfig(app);
            }
        }

        // Only validate the app they have set config for
        final Map<String, List<ConstraintViolationBean>> violations = validateConfig(configMap);
        if (violations.isEmpty()) {
            final File stagingDir = ctx.cloudOs.getStagingDirFile();
            final File databagsDir = new File(stagingDir, ChefSolo.DATABAGS_DIR);
            for (Map.Entry<String, AppConfiguration> entry : configMap.getAppConfigs().entrySet()) {
                final String appName = entry.getKey();
                final AppConfiguration config = entry.getValue();

                final File appDatabagDir = new File(databagsDir, appName);
                final File manifestFile = new File(appDatabagDir, AppManifest.CLOUDOS_MANIFEST_JSON);
                if (!manifestFile.exists()) return invalid("{err.cloudos.setConfig.missingManifest}");
                config.writeAppConfiguration(AppManifest.load(manifestFile), appDatabagDir);
            }

            // refresh config
            final String locale = ctx.admin.getLocale();
            configMap = getAppConfiguration(ctx.cloudOs, locale);
            validateConfig(configMap);
            return ok(configMap);
        } else {
            return invalidConfig(configMap);
        }

    }

    /**
     * Launch an instance.
     * @param apiKey The session ID
     * @param name The name of the instance
     * @return A CloudOsStatus object showing the status of the launch
     * @statuscode 422 If there were configuration errors that prevented the launch
     */
    @POST
    @Path("/{name}/launch")
    @ReturnType("cloudos.cloudstead.service.cloudos.CloudOsStatus")
    public Response launch (@HeaderParam(ApiConstants.H_API_KEY) String apiKey,
                            @PathParam("name") String name) {
        final CloudOsContext ctx = new CloudOsContext(apiKey, name);
        if (ctx.response != null) return ctx.response;

        if (ctx.cloudOs.getState() != CloudOsState.initial) return invalid("{err.cloudos.launch.notInitial}");

        // validate that all required configuration options have a value
        // we skip 'cloudos' and 'email' apps since they are configured after the cloudstead has an IP address
        final AppConfigurationMap configMap = getAppConfiguration(ctx.cloudOs, ctx.admin.getLocale());
        final Map<String, List<ConstraintViolationBean>> violations = validateConfig(configMap);
        if (!violations.isEmpty()) return invalidConfig(configMap);

        // this should return quickly with a status of pending
        final CloudOsStatus status = launchManager.launch(ctx.admin, ctx.cloudOs);

        return ok(status);
    }

    /**
     * View history for a CloudOs
     * @param apiKey The session ID
     * @param name The name of the instance
     * @return an updated CloudOsStatus object
     * @statuscode 403 instance is owned by another user
     * @statuscode 404 instance not found
     */
    @GET
    @Path("/{name}/status")
    @ReturnType("cloudos.cloudstead.service.cloudos.CloudOsStatus")
    public Response viewHistory(@HeaderParam(ApiConstants.H_API_KEY) String apiKey,
                                @PathParam("name") String name) {
        final CloudOsContext ctx = new CloudOsContext(apiKey, name);
        if (ctx.response != null) return ctx.response;

        final List<CloudOsEvent> events = eventDAO.findByCloudOs(ctx.cloudOs.getUuid());
        CloudOsStatus status = new CloudOsStatus(ctx.admin, ctx.cloudOs);
        status.setHistory(events);

        return ok(status);
    }

    /**
     * Destroy a CloudOs instance
     * @param apiKey The session ID
     * @param name the name of the instance
     * @return "true" if the teardown request was successfully started
     * @statuscode 403 instance is owned by another user
     * @statuscode 404 instance not found
     */
    @DELETE
    @Path("/{name}")
    @ReturnType("java.lang.Boolean")
    public Response delete (@HeaderParam(ApiConstants.H_API_KEY) String apiKey,
                            @PathParam("name") String name) {
        final CloudOsContext ctx = new CloudOsContext(apiKey, name);
        if (ctx.response != null) return ctx.response;

        // nothing to destroy? OK then, everything is fine
        if (ctx.cloudOs.getInstance() == null) {
            ctx.cloudOs.setState(CloudOsState.deleting);
            cloudOsDAO.delete(ctx.cloudOs.getUuid());
            return ok(Boolean.TRUE);
        }

        // todo: check cloudos.state -- can we delete from *any* state? or only certain ones?

        ctx.cloudOs.setState(CloudOsState.deleting);
        cloudOsDAO.update(ctx.cloudOs);

        launchManager.teardown(ctx.admin, ctx.cloudOs);

        return ok(Boolean.TRUE);
    }

    public AppConfigurationMap getAppConfiguration(CloudOs cloudOs, String locale) {
        final File stagingDir = cloudOs.getStagingDirFile();
        final AppConfigurationMap configMap = new AppConfigurationMap();
        configMap.addAll(cloudOs.getAllApps(), stagingDir, locale);
        return configMap;
    }

    public static final List<String> DATA_FILES = Arrays.asList(new String[] { "geoip" });

    public boolean prepChefStagingDir(CloudOs cloudOs) {

        try {
            final File stagingDir = cloudOs.initStagingDir(configuration.getCloudConfig().getChefStagingDir());
            final Map<AppLevel, List<String>> appsByLevel = new HashMap<>();
            for (String app : cloudOs.getAllApps()) {
                // get the latest version from app store
                final File bundleTarball = configuration.getLatestAppBundle(app);
                if (bundleTarball == null) {
                    throw new SimpleViolationException("err.cloudos.app.invalid", "no bundle could be located for app: "+app, app);
                }

                // unroll it, we'll rsync it to the target host later
                final File tempDir = Tarball.unroll(bundleTarball);

                // for now, rsync it to our staging directory
                CommandShell.execScript("rsync -avc " + abs(tempDir) + "/chef/* " + abs(stagingDir) + "/");

                // peek in the manifest to see what precedence this app should take in the run list
                final AppManifest manifest = AppManifest.load(tempDir);
                List<String> apps = appsByLevel.get(manifest.getLevel());
                if (apps == null) {
                    apps = new ArrayList<>(); appsByLevel.put(manifest.getLevel(), apps);
                }
                apps.add(app);
            }

            // Copy master files
            final File chefMaster = configuration.getCloudConfig().getChefMaster();
            for (File f : listFiles(chefMaster)) {
                FileUtils.copyFileToDirectory(f, stagingDir);
                if (f.canExecute()) chmod(new File(stagingDir, f.getName()), "a+rx");
            }

            // create data_files dir and copy files, if any
            final File masterDatafiles  = new File(chefMaster, ChefSolo.DATAFILES_DIR);
            final File stagingDatafiles = new File(stagingDir, ChefSolo.DATAFILES_DIR);
            for (String path : DATA_FILES) {
                CommandShell.exec("rsync -avzc "+abs(masterDatafiles)+"/"+path+" "+abs(stagingDatafiles));
            }

            // Order apps by level
            final List<String> allApps = new ArrayList<>(appsByLevel.size());
            for (AppLevel level : AppLevel.values()) {
                final List<String> apps = appsByLevel.get(level);
                if (apps != null) allApps.addAll(apps);
            }

            // Build chef solo.json using cookbooks in order of priority
            final ChefSolo soloJson = new ChefSolo();
            for (String app : allApps) {
                if (ChefSolo.recipeExists(stagingDir, app, "lib")) soloJson.add("recipe["+app+"::lib]");
            }
            for (String app : allApps) {
                if (ChefSolo.recipeExists(stagingDir, app, "default")) {
                    soloJson.add("recipe[" + app + "]");
                } else {
                    log.warn("No default recipe found for app: "+app);
                }
            }
            for (String app : allApps) {
                if (ChefSolo.recipeExists(stagingDir, app, "validate")) soloJson.add("recipe["+app+"::validate]");
            }
            soloJson.write(stagingDir);

        } catch (Exception e) {
            log.error("prepChefStagingDir: Error preparing chef staging dir: "+e);
            return false;
        }
        return true;
    }

    private class CloudOsContext {
        public Admin admin;
        public CloudOs cloudOs;

        public Response response = null;

        public CloudOsContext(String apiKey, String name) {
            admin = sessionDAO.find(apiKey);
            if (admin == null) {
                response = forbidden();

            } else if (!empty(name)) {
                cloudOs = cloudOsDAO.findByName(name);
                if (cloudOs == null) {
                    response = notFound(name);

                } else if (!admin.isAdmin() && !cloudOs.getAdminUuid().equals(admin.getUuid())) {
                    // must be admin or own the cloudos to operate upon it
                    response = forbidden();
                }
            }
        }
    }
}
