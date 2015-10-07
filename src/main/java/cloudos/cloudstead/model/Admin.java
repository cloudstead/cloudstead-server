package cloudos.cloudstead.model;

import cloudos.cloudstead.model.support.AdminRequest;
import cloudos.model.AccountBase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.wizard.validation.IsUnique;

import javax.persistence.Column;
import javax.persistence.Entity;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

@Entity
@IsUnique.List({
        @IsUnique(unique="email",       daoBean="adminDAO", message="{err.email.notUnique}"),
        @IsUnique(unique="mobilePhone", daoBean="adminDAO", message="{err.mobilePhone.notUnique}")
})
public class Admin extends AccountBase {

    @Override public void beforeCreate() { if (!hasUuid()) die("no uuid assigned"); }

    @Override public AccountBase setEmail(String email) {
        super.setName(email);
        return super.setEmail(email);
    }

    @JsonIgnore @Getter @Setter private Integer tosVersion;

    public Admin populate(AdminRequest request) {
        super.populate(request);
        setTosVersion(request.isTos() ? 1 : null); // todo: get TOS version from TOS service/dao. for now default to version 1
        return this;
    }

    @Column(nullable=false, updatable=false)
    @Getter @Setter private int maxCloudsteads = 1;
}
