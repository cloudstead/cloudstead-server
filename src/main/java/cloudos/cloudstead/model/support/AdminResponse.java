package cloudos.cloudstead.model.support;

import cloudos.cloudstead.model.Admin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cobbzilla.util.reflect.ReflectionUtil;

@NoArgsConstructor
public class AdminResponse extends Admin {

    @Getter @Setter private String session;

    public AdminResponse(Admin admin, String sessionId) {
        ReflectionUtil.copy(this, admin);
        setSession(sessionId);
    }

}
