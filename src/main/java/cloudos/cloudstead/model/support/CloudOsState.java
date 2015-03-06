package cloudos.cloudstead.model.support;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum CloudOsState {

    initial, retrying, starting, started, cheffing, setup_complete, live, error, destroying, destroyed, deleting, lost;

    @JsonCreator public CloudOsState create (String name) { return valueOf(name.toLowerCase()); }

}
