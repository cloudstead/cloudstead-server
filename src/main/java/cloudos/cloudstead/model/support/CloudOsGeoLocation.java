package cloudos.cloudstead.model.support;

import com.fasterxml.jackson.annotation.JsonCreator;

import static org.cobbzilla.util.string.StringUtil.empty;

public enum CloudOsGeoLocation {

    us_east, us_west, eu, singapore;

    @JsonCreator public CloudOsGeoLocation create (String val) {
        return empty(val) ? null : CloudOsGeoLocation.valueOf(val.toLowerCase());
    }
}
