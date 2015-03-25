package cloudos.cloudstead.model.support;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum CloudOsInstanceOption {

    bundles, editions, regions;

    @JsonCreator public CloudOsInstanceOption create (String val) { return valueOf(val.toLowerCase()); }

}
