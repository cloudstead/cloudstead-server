package cloudos.cloudstead.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.wizard.model.HashedPassword;
import org.cobbzilla.wizard.model.IdentifiableBase;
import org.cobbzilla.wizard.validation.IsUnique;

import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
@IsUnique.List({
        @IsUnique(unique="email",       daoBean="adminDAO", message="{err.email.notUnique}"),
        @IsUnique(unique="mobilePhone", daoBean="adminDAO", message="{err.mobilePhone.notUnique}")
})
public class Admin extends IdentifiableBase {

    @Getter @Setter @Embedded @JsonIgnore private HashedPassword password;

    @Getter private String email;
    @JsonIgnore @Getter @Setter private String emailVerificationCode;
    @JsonIgnore @Getter @Setter private Long emailVerificationCodeCreatedAt;
    @Getter @Setter private boolean emailVerified = false;

    public void setEmail (String email) {
        if (this.email == null || !this.email.equals(email)) {
            emailVerified = false;
            emailVerificationCode = null;
            emailVerificationCodeCreatedAt = null;
        }
        this.email = email;
    }

    @Getter private String mobilePhone;
    @JsonIgnore @Getter @Setter private String mobilePhoneVerificationCode;
    @JsonIgnore @Getter @Setter private Long mobilePhoneVerificationCodeCreatedAt;
    @Getter @Setter private boolean mobilePhoneVerified = false;

    public void setMobilePhone (String mobilePhone) {
        if (this.mobilePhone == null || !this.mobilePhone.equals(mobilePhone)) {
            mobilePhoneVerified = false;
            mobilePhoneVerificationCode = null;
            mobilePhoneVerificationCodeCreatedAt = null;
        }
        this.mobilePhone = mobilePhone;
    }

}
