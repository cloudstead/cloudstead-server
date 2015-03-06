package cloudos.cloudstead.model.support;

import com.fasterxml.jackson.annotation.JsonCreator;

import static org.cobbzilla.util.string.StringUtil.empty;

// todo: these should eventually be managed in the DB
public enum CloudOsGeoRegion {

    us_east, us_west, eu, singapore;

    @JsonCreator public CloudOsGeoRegion create (String val) {
        return empty(val) ? null : CloudOsGeoRegion.valueOf(val.toLowerCase());
    }
}
