package cloudos.cloudstead.model;

import cloudos.cloudstead.model.support.*;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.cslib.compute.instance.CsInstance;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.cobbzilla.util.collection.ListUtil;
import org.cobbzilla.util.json.JsonUtil;
import org.cobbzilla.wizard.model.UniquelyNamedEntity;
import org.cobbzilla.wizard.validation.IsUnique;
import org.cobbzilla.wizard.validation.SimpleViolationException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.security.ShaUtil.sha256_hex;
import static org.cobbzilla.util.string.StringUtil.empty;

@Entity
@IsUnique(unique="name", daoBean="cloudOsDAO", message="{err.name.notUnique}")
@ToString(of={"adminUuid", "name", "state", "instanceJson"})
public class CloudOs extends UniquelyNamedEntity {

    @Getter @Setter private String adminUuid;

    // Name has a lot of restrictions: must have a value; min 3/max 30 alphanumeric chars; cannot be reserved word
    @Size(max=30, message="err.cloudos.name.length")
    @Pattern(regexp = "[A-Za-z0-9]{3,}", message = "err.cloudos.name.invalid")
    @Column(updatable=false, unique=true, length=30)
    public String getName () { return name == null ? null : name.toLowerCase(); }
    public CloudOs setName (String n) {
        if (ReservedCloudOsNames.isReserved(n)) throw new SimpleViolationException("err.cloudos.name.reserved");
        name = (n == null) ? null : n.toLowerCase(); return this;
    }

    @NotNull @Enumerated(value=EnumType.STRING) @Column(length=30, nullable=false)
    @Getter @Setter private CloudOsEdition edition;

    @NotNull @Enumerated(value=EnumType.STRING) @Column(length=30, nullable=false)
    @Getter @Setter private CloudOsAppBundle appBundle;

    @NotNull @Enumerated(value=EnumType.STRING) @Column(length=30, nullable=false)
    @Getter @Setter private CloudOsGeoRegion region;

    @Size(max=1024, message="err.cloudos.additionalApps.length")
    @Getter @Setter private String additionalApps;

    @JsonIgnore public List<String> getAdditionalAppsList () { return Arrays.asList(additionalApps.split("[,\\s]+")); }

    @JsonIgnore public List<String> getAllApps() {
        return empty(additionalApps)
                ? appBundle.getApps()
                : ListUtil.concat(appBundle.getApps(), getAdditionalAppsList());
    }

    @NotNull @Enumerated(value=EnumType.STRING) @Column(length=30, nullable=false)
    @Getter @Setter private CloudOsState state = CloudOsState.initial;
    @Getter @Setter private long lastStateChange;

    @JsonIgnore public boolean isRunning() { return state == CloudOsState.live; }

    public void updateState (CloudOsState newState) {
        if (newState != state) {
            state = newState;
            lastStateChange = System.currentTimeMillis();
        }
    }

    @Column(nullable=false, updatable=false, unique=true, length=100)
    @Getter @Setter private String ucid;
    public void initUcid () { if (empty(ucid)) this.ucid = UUID.randomUUID().toString(); }

    @Size(max=2048, message="err.cloudos.instanceJson.tooLong")
    @Getter @Setter @JsonIgnore private String instanceJson;

    @JsonIgnore public CsInstance getInstance () {
        try {
            return instanceJson == null ? null : JsonUtil.fromJson(instanceJson, CsInstance.class);
        } catch (Exception e) {
            return die("Invalid instanceJson: " + instanceJson);
        }
    }

    // for backing up instance configuration and data to S3
    // all backups are encrypted on the instance with the end-user's master password
    // so while cloudstead can read the data, it will be indecipherable to us
    @Getter @Setter @JsonIgnore private String s3accessKey;
    @Getter @Setter @JsonIgnore private String s3secretKey;

    public static String getIAMpath(Admin admin) {
        return "/cloudos/" + admin.getUuid().replace("-", "") + "/";
    }

    public static String getIAMuser(Admin admin, String hostname, String salt) {
        return admin.getUuid().replace("-", "") + "_" + sha256_hex(hostname + salt).substring(0, 10);
    }

    public static String getSendgridUser(Admin admin, String hostname, String salt) {
        return getIAMuser(admin, hostname, salt).replace("_", "");
    }

    public void populate(Admin admin, CloudOsRequest request) {
        setAdminUuid(admin.getUuid());
        setName(request.getName());
        setEdition(request.getEdition());
        setRegion(request.getRegion());
        setAppBundle(request.getAppBundle());
        setAdditionalApps(request.getAdditionalApps());
        initUcid();
    }

    public File getStagingDir(CloudsteadConfiguration configuration) {
        return configuration.getCloudConfig().getChefStagingDir(this);
    }
}
