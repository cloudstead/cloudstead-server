package cloudos.cloudstead.resources;

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
import cloudos.cloudstead.server.CloudConfiguration;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.cloudstead.service.cloudos.CloudOsLaunchManager;
import cloudos.cloudstead.service.cloudos.CloudOsStatus;
import cloudos.cloudstead.service.cloudos.CloudsteadConfigValidationResolver;
import com.qmino.miredot.annotations.ReturnType;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.wizard.resources.ResourceUtil;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rooty.toots.chef.ChefSolo;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path(ApiConstants.CLOUDOS_ENDPOINT)
@Service @Slf4j
public class CloudOsResource {

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

        final Admin admin = sessionDAO.find(apiKey);
        if (admin == null) return ResourceUtil.forbidden();

        final List<CloudOs> found = cloudOsDAO.findByAdmin(admin.getUuid());
        return Response.ok(found).build();
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

        final Admin admin = sessionDAO.find(apiKey);
        if (admin == null) return ResourceUtil.forbidden();

        final CloudOs cloudOs = cloudOsDAO.findByName(name);
        if (cloudOs == null) return ResourceUtil.notFound(name);
        if (!cloudOs.getAdminUuid().equals(admin.getUuid())) return ResourceUtil.forbidden();

        return Response.ok(cloudOs).build();
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
                            CloudOsRequest request) {
        Admin admin = sessionDAO.find(apiKey);
        if (admin == null) return ResourceUtil.forbidden();

        // sanity check
        if (!name.equalsIgnoreCase(request.getName())) return ResourceUtil.invalid();

        // must be activated
        admin = adminDAO.findByUuid(admin.getUuid());
        if (!admin.isEmailVerified()) return ResourceUtil.invalid("{err.cloudos.create.unverifiedEmail}");

        // non-admins: cannot create more than max # of cloudsteads
        if (!admin.isAdmin() && cloudOsDAO.findByAdmin(admin.getUuid()).size() >= admin.getMaxCloudsteads()) {
            return ResourceUtil.invalid("{err.cloudos.create.maxCloudsteads}");
        }

        if (cloudOsDAO.findByName(name) != null) return ResourceUtil.invalid("{err.cloudos.create.name.notUnique}");

        CloudOs cloudOs = new CloudOs();
        cloudOs.populate(admin, request);
        cloudOs = cloudOsDAO.create(cloudOs);

        if (prepChefStagingDir(cloudOs)) return Response.serverError().build();

        return Response.ok(cloudOs).build();
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
        Admin admin = sessionDAO.find(apiKey);
        if (admin == null) return ResourceUtil.forbidden();

        CloudOs cloudOs = cloudOsDAO.findByName(name);
        if (cloudOs == null) return ResourceUtil.notFound(name);

        // to continue, user must be superadmin, or owner of the cloudos
        if (!admin.isAdmin() && !cloudOs.getAdminUuid().equals(admin.getUuid())) return ResourceUtil.forbidden();

        if (cloudOs.getState() != CloudOsState.initial) return ResourceUtil.invalid("{err.cloudos.update.notInitial}");

        // determine if the list of apps has changed
        final List<String> requestApps = request.getAllApps();
        final List<String> currentApps = cloudOs.getAllApps();
        final boolean sameApps = requestApps.containsAll(currentApps) && currentApps.containsAll(requestApps);

        cloudOs.populate(admin, request);
        cloudOs = cloudOsDAO.update(cloudOs);

        // app list has changed, update chef staging directory
        if (!sameApps) {
            try {
                prepChefStagingDir(cloudOs);
            } catch (Exception e) {
                log.error("Error preparing chef staging dir: " + e);
                return Response.serverError().build();
            }
        }

        return Response.ok(cloudOs).build();
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
        final Admin admin = sessionDAO.find(apiKey);
        if (admin == null) return ResourceUtil.forbidden();

        final CloudOs cloudOs = cloudOsDAO.findByName(name);
        if (cloudOs == null) return ResourceUtil.notFound();
        if (!cloudOs.getAdminUuid().equals(admin.getUuid())) return ResourceUtil.forbidden();

        if (cloudOs.getState() != CloudOsState.initial) return ResourceUtil.invalid("{err.cloudos.getConfig.notInitial}");

        final String locale = admin.getLocale();
        final AppConfigurationMap configMap = getAppConfiguration(cloudOs, locale);

        return Response.ok(configMap).build();
    }

    /**
     * Update the initial configuration for a cloudstead.
     * @param apiKey The session ID
     * @param name The name of the instance
     * @param configMap The configuration updates. Apps that are not present in this map will not be updated.
     * @return The AppConfigurationMap, containing configs for all apps to be installed
     */
    @POST
    @Path("/{name}/config")
    @ReturnType("cloudos.appstore.model.app.config.AppConfigurationMap")
    public Response setConfig (@HeaderParam(ApiConstants.H_API_KEY) String apiKey,
                               @PathParam("name") String name,
                               AppConfigurationMap configMap) {
        final Admin admin = sessionDAO.find(apiKey);
        if (admin == null) return ResourceUtil.forbidden();

        final CloudOs cloudOs = cloudOsDAO.findByName(name);
        if (cloudOs == null) return ResourceUtil.notFound();
        if (!cloudOs.getAdminUuid().equals(admin.getUuid())) return ResourceUtil.forbidden();

        if (cloudOs.getState() != CloudOsState.initial) return ResourceUtil.invalid("{err.cloudos.setConfig.notInitial}");

        final File stagingDir = configuration.getCloudConfig().getChefStagingDir(cloudOs);
        final File databagsDir = new File(stagingDir, ChefSolo.DATABAGS_DIR);
        for (Map.Entry<String, AppConfiguration> entry : configMap.getAppConfigs().entrySet()) {
            final File appDatabagDir = new File(databagsDir, entry.getKey());
            final File manifestFile = new File(appDatabagDir, AppManifest.CLOUDOS_MANIFEST_JSON);
            if (!manifestFile.exists()) return ResourceUtil.invalid("{err.cloudos.setConfig.missingManifest}");
            AppConfiguration.setAppConfiguration(AppManifest.load(manifestFile), appDatabagDir, entry.getValue());
        }

        // refresh config
        final String locale = admin.getLocale();
        configMap = getAppConfiguration(cloudOs, locale);

        return Response.ok(configMap).build();
    }

    /**
     * Launch an instance.
     * @param apiKey The session ID
     * @param name The name of the instance
     * @return A CloudOsStatus object showing the status of the launch
     */
    @POST
    @Path("/{name}/launch")
    @ReturnType("cloudos.cloudstead.service.cloudos.CloudOsStatus")
    public Response launch (@HeaderParam(ApiConstants.H_API_KEY) String apiKey,
                            @PathParam("name") String name) {

        final Admin admin = sessionDAO.find(apiKey);
        if (admin == null) return ResourceUtil.forbidden();

        final CloudOs cloudOs = cloudOsDAO.findByName(name);
        if (cloudOs == null) return ResourceUtil.notFound();
        if (!admin.isAdmin() && !cloudOs.getAdminUuid().equals(admin.getUuid())) return ResourceUtil.forbidden();

        // validate that all required configuration options have a value
        final List<ConstraintViolationBean> violations = new ArrayList<>();
        final AppConfigurationMap configMap = getAppConfiguration(cloudOs, null);
        for (Map.Entry<String, AppConfiguration> entry : configMap.getAppConfigs().entrySet()) {
            entry.getValue().validate(violations, CloudsteadConfigValidationResolver.INSTANCE);
        }

        if (!violations.isEmpty()) return ResourceUtil.invalid(violations);

        // this should return quickly with a status of pending
        final CloudOsStatus status = launchManager.launch(admin, cloudOs);

        return Response.ok(status).build();
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
    public Response status(@HeaderParam(ApiConstants.H_API_KEY) String apiKey,
                           @PathParam("name") String name) {

        final Admin admin = sessionDAO.find(apiKey);
        if (admin == null) return ResourceUtil.forbidden();

        final CloudOs cloudOs = cloudOsDAO.findByName(name);
        if (cloudOs == null) return ResourceUtil.notFound();
        if (!admin.isAdmin() && !cloudOs.getAdminUuid().equals(admin.getUuid())) return ResourceUtil.forbidden();

        final List<CloudOsEvent> events = eventDAO.findByCloudOs(cloudOs.getUuid());
        CloudOsStatus status = new CloudOsStatus(admin, cloudOs);
        status.setHistory(events);

        return Response.ok(status).build();
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
        final Admin admin = sessionDAO.find(apiKey);
        if (admin == null) return ResourceUtil.forbidden();

        final CloudOs cloudOs = cloudOsDAO.findByName(name);
        if (cloudOs == null) return ResourceUtil.notFound();
        if (!cloudOs.getAdminUuid().equals(admin.getUuid())) return ResourceUtil.forbidden();

        cloudOs.setState(CloudOsState.deleting);
        cloudOsDAO.update(cloudOs);
        launchManager.teardown(admin, cloudOs);

        return Response.ok(Boolean.TRUE).build();
    }

    public AppConfigurationMap getAppConfiguration(CloudOs cloudOs, String locale) {
        final File stagingDir = configuration.getCloudConfig().getChefStagingDir(cloudOs);
        final AppConfigurationMap configMap = new AppConfigurationMap();
        configMap.addAll(cloudOs.getAllApps(), stagingDir, locale);
        return configMap;
    }

    public boolean prepChefStagingDir(CloudOs cloudOs) {
        final CloudConfiguration cloudConfig = configuration.getCloudConfig();
        final File chefMaster = cloudConfig.getChefDir();
        final File stagingDir = cloudConfig.getChefStagingDir(cloudOs);
        try {
            ChefSolo.prepareChefStagingDir(cloudOs.getAllApps(), chefMaster, stagingDir);
        } catch (Exception e) {
            log.error("Error preparing chef staging dir: "+e);
            return true;
        }
        return false;
    }

}
