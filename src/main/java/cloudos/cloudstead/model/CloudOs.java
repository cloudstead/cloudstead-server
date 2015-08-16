package cloudos.cloudstead.model;

import cloudos.model.instance.CloudOsAppBundle;
import cloudos.cloudstead.model.support.CloudOsEdition;
import cloudos.cloudstead.model.support.CloudOsRequest;
import cloudos.model.AccountBase;
import cloudos.model.instance.CloudOsBase;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.collection.ListUtil;
import org.cobbzilla.wizard.model.Identifiable;
import org.cobbzilla.wizard.validation.IsUnique;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.security.ShaUtil.sha256_hex;

@Entity
@IsUnique(unique="name", daoBean="cloudOsDAO", message="{err.name.notUnique}")
public class CloudOs extends CloudOsBase {

    @Override protected boolean isReserved(String n) { return ReservedCloudOsNames.isReserved(n); }

    @NotNull(message="err.cloudos.edition.required")
    @Enumerated(value=EnumType.STRING)
    @Column(length=30, nullable=false)
    @Getter @Setter private CloudOsEdition edition;

    @Override public String getInstanceType() {
        // instance type depends on the 'edition'
        return edition.getInstanceType(getCsRegion().getCloudVendor());
    }

    @NotNull(message="err.cloudos.appBundle.required")
    @Enumerated(value=EnumType.STRING)
    @Column(length=30, nullable=false)
    @Getter @Setter private CloudOsAppBundle appBundle;

    @Override public List<String> getAllApps() {
        return empty(getApps())
                ? appBundle.getApps()
                : ListUtil.concat(appBundle.getApps(), super.getAllApps());
    }

    @Override public String getIAMpath(Identifiable admin) {
        return "/cloudos/" + admin.getUuid().replace("-", "") + "/";
    }

    @Override public String getIAMuser(Identifiable admin, String hostname, String salt) {
        return admin.getUuid().replace("-", "") + "_" + sha256_hex(hostname + salt).substring(0, 10);
    }

    public String getSendgridUser(Identifiable admin, String hostname, String salt) {
        return getIAMuser(admin, hostname, salt).replace("_", "");
    }

    public void populate(AccountBase admin, CloudOsRequest request) {
        setAdminUuid(admin.getUuid());
        setName(request.getName());
        setEdition(request.getEdition());
        setCsRegion(request.getRegion());
        setAppBundle(request.getAppBundle());
        setApps(request.getAdditionalApps());
        setInstanceType(request.getEdition().getInstanceType(getCsRegion().getCloudVendor()));
        initUcid();
    }
}
