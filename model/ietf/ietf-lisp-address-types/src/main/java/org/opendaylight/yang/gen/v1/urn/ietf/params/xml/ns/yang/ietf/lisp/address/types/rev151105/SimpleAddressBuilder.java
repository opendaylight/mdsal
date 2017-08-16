package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105;


import com.google.common.base.Preconditions;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;

/**
 * The purpose of generated class in src/main/java for Union types is to create new instances of unions from a string representation.
 * In some cases it is very difficult to automate it since there can be unions such as (uint32 - uint16), or (string - uint32).
 *
 * The reason behind putting it under src/main/java is:
 * This class is generated in form of a stub and needs to be finished by the user. This class is generated only once to prevent
 * loss of user code.
 *
 */
public class SimpleAddressBuilder {

    public static SimpleAddress getDefaultInstance(String defaultValue) {
        Preconditions.checkNotNull(defaultValue, "Cannot convert null address");

        if (Ipv4Matcher.matches(defaultValue)) {
            return new SimpleAddress(new IpAddress(new Ipv4Address(defaultValue)));
        } else if (Ipv6Matcher.matches(defaultValue)) {
            return new SimpleAddress(new IpAddress(new Ipv6Address(defaultValue)));
        } else if (MacMatcher.matches(defaultValue)) {
            return new SimpleAddress(new MacAddress(defaultValue));
        } else {
            throw new IllegalArgumentException("Unknown type");
        }
    }

}
