package cloudos.cloudstead.model.support;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cobbzilla.wizard.validation.HasValue;
import org.cobbzilla.wizard.validation.NotReservedWord;
import cloudos.cloudstead.model.ReservedCloudOsNames;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@NoArgsConstructor @AllArgsConstructor
public class CloudOsRequest {

    // Name has a lot of restrictions: must have a value; min 3/max 20 alphanumeric chars; cannot be reserved word
    @HasValue(message = "error.cloudosRequest.name.required")
    @Size(max=20, message="error.cloudosRequest.name.tooLong")
    @Pattern(regexp = "[A-Za-z0-9]{3,}", message = "error.cloudosRequest.name.invalid")
    @NotReservedWord(reserved=ReservedCloudOsNames.class, message = "error.cloudosRequest.name.reserved")
    @Getter @Setter private String name;

}
