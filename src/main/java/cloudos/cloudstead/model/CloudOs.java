package cloudos.cloudstead.model;

import cloudos.cslib.compute.instance.CsInstance;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.cobbzilla.util.json.JsonUtil;
import org.cobbzilla.wizard.model.UniquelyNamedEntity;
import org.cobbzilla.wizard.validation.IsUnique;
import org.cobbzilla.wizard.validation.SimpleViolationException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import static org.cobbzilla.util.security.ShaUtil.sha256_hex;

@Entity
@IsUnique(unique="name", daoBean="cloudOsDAO", message="{err.name.notUnique}")
@ToString(of={"adminUuid", "name", "running", "instanceJson"})
public class CloudOs extends UniquelyNamedEntity {

    @Getter @Setter private String adminUuid;

    // Name has a lot of restrictions: must have a value; min 3/max 30 alphanumeric chars; cannot be reserved word
    @Size(max=30, message="error.cloudosRequest.name.tooLong")
    @Pattern(regexp = "[A-Za-z0-9]{3,}", message = "error.cloudosRequest.name.invalid")
    @Column(updatable=false, unique=true, length=30)
    public String getName () { return name == null ? null : name.toLowerCase(); }
    public CloudOs setName (String n) {
        if (ReservedCloudOsNames.isReserved(n)) throw new SimpleViolationException("err.name.reserved");
        name = (n == null) ? null : n.toLowerCase(); return this;
    }

    @Getter @Setter private boolean running = false;

    @Size(max=1024, message="error.cloudOsRequest.instanceJson.tooLong")
    @Getter @Setter @JsonIgnore private String instanceJson;

    @JsonIgnore public CsInstance getInstance () {
        try {
            return instanceJson == null ? null : JsonUtil.fromJson(instanceJson, CsInstance.class);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid instanceJson: "+instanceJson);
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

}
