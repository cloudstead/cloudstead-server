package cloudos.cloudstead.model.support;

import com.fasterxml.jackson.annotation.JsonCreator;

import static org.cobbzilla.util.string.StringUtil.empty;

public enum CloudOsEdition {

    starter, master, business, king, emperor, maximus, supermax;

    @JsonCreator public static CloudOsEdition create (String val) {
        return empty(val) ? null : CloudOsEdition.valueOf(val.toLowerCase());
    }
}
