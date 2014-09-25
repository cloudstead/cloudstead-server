package cloudos.cloudstead.model.support;

import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.wizard.validation.IsUnique;

@IsUnique.List({
        @IsUnique(unique="email",       daoBean="adminDAO", message="{err.email.notUnique}"),
        @IsUnique(unique="mobilePhone", daoBean="adminDAO", message="{err.mobilePhone.notUnique}")
})
public class AdminRequest {

    // todo: add field validations
    @Getter @Setter private String uuid;
    @Getter @Setter private String email;
    @Getter @Setter private String mobilePhone;
    @Getter @Setter private String password;

}
