package cloudos.cloudstead.model;

import cloudos.model.AccountBase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.wizard.model.HashedPassword;
import org.cobbzilla.wizard.validation.IsUnique;

import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
@IsUnique.List({
        @IsUnique(unique="email",       daoBean="adminDAO", message="{err.email.notUnique}"),
        @IsUnique(unique="mobilePhone", daoBean="adminDAO", message="{err.mobilePhone.notUnique}")
})
public class Admin extends AccountBase {

    @Getter @Setter @Embedded @JsonIgnore private HashedPassword hashedPassword;

    @Override
    public AccountBase setEmail(String email) {
        super.setName(email);
        return super.setEmail(email);
    }

    @Getter @Setter private Integer tosVersion;

}
