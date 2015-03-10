package cloudos.cloudstead.main;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum CloudOsOperation {

    list, view, create, update, config, launch, destroy;

    @JsonCreator public static CloudOsOperation create (String name) { return valueOf(name.toLowerCase()); }

}
