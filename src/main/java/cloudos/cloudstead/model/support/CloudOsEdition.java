package cloudos.cloudstead.model.support;

import cloudos.cslib.compute.CsCloud;
import cloudos.cslib.compute.meta.CsCloudType;
import cloudos.model.CsInstanceType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

import static cloudos.cslib.compute.meta.CsCloudTypeFactory.*;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

/**
 * This is where we maintain the mapping of edition->hardware profile, for CsCloudTypes supporting that edition.
 */
@AllArgsConstructor
public enum CloudOsEdition {

    starter   ((Map) ImmutableMap.of(
                    DO, DO.type("2gb"),
                    RS, RS.type("general1-1"))),

    master    ((Map) ImmutableMap.of(
                    EC2, EC2.type("m3.medium"),
                    DO, DO.type("4gb"),
                    RS, RS.type("general1-2"))),

    business   ((Map) ImmutableMap.of(
                    EC2, EC2.type("r3.large"),
                    DO, DO.type("8gb"),
                    RS, RS.type("compute1-4"))),

    king       ((Map) ImmutableMap.of(
                    EC2, EC2.type("r3.xlarge"),
                    DO, DO.type("16gb"),
                    RS, RS.type("compute1-8"))),

    emperor    ((Map) ImmutableMap.of(
                    EC2, EC2.type("r3.2xlarge"),
                    DO, DO.type("32gb"),
                    RS, RS.type("compute1-15"))),

    maximus    ((Map) ImmutableMap.of(
                    EC2, EC2.type("r3.4xlarge"),
                    DO, DO.type("48gb"),
                    RS, RS.type("compute1-30"))),

    supermax   ((Map) ImmutableMap.of(
                    EC2, EC2.type("r3.8xlarge"),
                    DO, DO.type("64gb"),
                    RS, RS.type("compute1-60")));

    @Getter private final Map<CsCloudType<? extends CsCloud>, CsInstanceType> instanceTypes;

    @JsonCreator public static CloudOsEdition create (String val) { return empty(val) ? null : valueOf(val.toLowerCase()); }

    public String getInstanceType(CsCloudType cloudVendor) {
        final CsInstanceType type = instanceTypes.get(cloudVendor);
        if (type != null) return type.getName();
        return die("Vendor " + cloudVendor.getName() + " not supported on edition " + this);
    }

    public boolean isValid(CsCloudType<? extends CsCloud> cloudType) {
        return instanceTypes.containsKey(cloudType);
    }
}
