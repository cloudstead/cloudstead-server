package cloudos.cloudstead.model.support;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cobbzilla.util.reflect.ReflectionUtil;
import cloudos.cloudstead.model.Admin;

@NoArgsConstructor
public class AdminResponse extends Admin {

    @Getter @Setter private String session;

    public AdminResponse(Admin admin, String sessionId) {
        ReflectionUtil.copy(this, admin);
        setSession(sessionId);
    }

}
