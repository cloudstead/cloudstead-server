package cloudos.cloudstead.model.support;

import cloudos.cloudstead.model.Admin;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.string.StringUtil;
import org.cobbzilla.wizard.validation.HasValue;
import org.cobbzilla.wizard.validation.IsUnique;

import javax.validation.constraints.Size;

@IsUnique.List({
        @IsUnique(unique="email",       daoBean="adminDAO", message="{err.email.notUnique}"),
        @IsUnique(unique="mobilePhone", daoBean="adminDAO", message="{err.mobilePhone.notUnique}")
})
public class AdminRequest extends Admin {

    // optional, if no password set, then one will be generated and user will be instructed to login and change it
    @Getter @Setter private String password;
    @JsonIgnore public boolean hasPassword() { return !StringUtil.empty(password); }

    @HasValue(message="{err.activationCode.empty}")
    @Size(max=UUID_MAXLEN, message="{err.activationCode.length}")
    @Getter @Setter private String activationCode;

    @HasValue(message="err.tos.empty")
    @Getter @Setter private boolean tos;

}
