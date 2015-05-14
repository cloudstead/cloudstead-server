package cloudos.cloudstead.model.support;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum CloudsteadEntityType {

    admin, cloudos, servicekey;

    @JsonCreator public static CloudsteadEntityType create(String value) { return valueOf(value.toLowerCase()); }

}
