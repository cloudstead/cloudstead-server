package cloudos.cloudstead.model.support;

import cloudos.cloudstead.model.ReservedCloudOsNames;
import cloudos.model.CsGeoRegion;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.util.collection.ListUtil;
import org.cobbzilla.wizard.validation.HasValue;
import org.cobbzilla.wizard.validation.NotReservedWord;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@NoArgsConstructor @AllArgsConstructor @Accessors(chain=true)
public class CloudOsRequest {

    public CloudOsRequest (String name) {
        setName(name);
    }

    // Name has a lot of restrictions: must have a value; min 3/max 20 alphanumeric chars; cannot be reserved word
    @HasValue(message = "err.cloudos.name.required")
    @Size(max=20, message="err.cloudos.name.length")
    @Pattern(regexp = "[A-Za-z0-9]{3,}", message = "err.cloudos.name.invalid")
    @NotReservedWord(reserved=ReservedCloudOsNames.class, message="{err.cloudos.name.reserved}")
    @Getter @Setter private String name;

    @HasValue(message = "err.cloudos.edition.required")
    @Getter @Setter private CloudOsEdition edition = CloudOsEdition.starter;

    @HasValue(message = "err.cloudos.appBundle.required")
    @Getter @Setter private CloudOsAppBundle appBundle = CloudOsAppBundle.basic;

    @Valid @HasValue(message = "err.cloudos.region.required")
    @Getter @Setter private CsGeoRegion region = null;

    @Getter @Setter private String additionalApps;

    @JsonIgnore public List<String> getAdditionalAppsList () { return Arrays.asList(additionalApps.split("[,\\s]+")); }

    @JsonIgnore public List<String> getAllApps() {
        return empty(additionalApps)
                ? appBundle.getApps()
                : ListUtil.concat(appBundle.getApps(), getAdditionalAppsList());
    }
}
