package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefixBuilder;
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
        SimpleAddress address = tryIpAddress(defaultValue);
        if (address != null) {
            return address;
        }

        address = tryIpPrefix(defaultValue);
        if (address != null) {
            return address;
        }

        address = tryMacAddress(defaultValue);
        if (address != null) {
            return address;
        }

        // XXX need support for MAC addresses and AS numbers
        address = new SimpleAddress(new DistinguishedNameType(defaultValue));

        return address;
    }

    private static SimpleAddress tryIpAddress(String defaultValue) {
        try {
            SimpleAddress address = new SimpleAddress(IpAddressBuilder.getDefaultInstance(defaultValue));
            return address;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static SimpleAddress tryIpPrefix(String defaultValue) {
        try {
            SimpleAddress address = new SimpleAddress(IpPrefixBuilder.getDefaultInstance(defaultValue));
            return address;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static SimpleAddress tryMacAddress(String defaultValue) {
        try {
            SimpleAddress address = new SimpleAddress(new MacAddress(defaultValue));
            return address;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
