package cloudos.cloudstead.main;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum CloudOsOperation {

    list, view, status, create, update, config, launch, destroy;

    @JsonCreator public static CloudOsOperation create (String name) { return valueOf(name.toLowerCase()); }

    public boolean requiresName() { return this != list && this != view; }

}
