package cloudos.cloudstead.model.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.util.string.StringUtil;

@Accessors(chain=true)
public class LoginRequest {

    @Getter @Setter private String email;
    @Getter @Setter private String password;
    @Getter @Setter private String secondFactor;
    @Getter @Setter private String deviceId;

    @JsonIgnore public boolean isSecondFactor () { return !StringUtil.empty(secondFactor); }

}
