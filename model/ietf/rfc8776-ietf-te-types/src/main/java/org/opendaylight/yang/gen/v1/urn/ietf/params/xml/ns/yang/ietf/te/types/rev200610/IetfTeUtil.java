/*
 * Copyright (c) 2020, 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.types.rev200610;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.HexString;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Utility methods for dealing with {@code ietf-te-types.yang} unions.
 */
public final class IetfTeUtil {
    private static final @NonNull TeTopologyId EMPTY_TE_TOPOLOGY_ID = new TeTopologyId("");

    private IetfTeUtil() {
        // Hidden on purpose
    }

    public static @NonNull AdminGroups admingGroupsFor(final String str) {
        final HexString hex = new HexString(str);
        return str.length() <= 11 ? new AdminGroups(new AdminGroup(hex)) : new AdminGroups(new ExtendedAdminGroup(hex));
    }

    public static @NonNull TeTopologyId teTopologyIdFor(final String str) {
        return str.isEmpty() ? EMPTY_TE_TOPOLOGY_ID : new TeTopologyId(str);
    }

    public static @NonNull TeTpId teTpIdFor(final String str) {
        return str.indexOf('.') == -1 && str.indexOf(':') == -1
                ? new TeTpId(Uint32.valueOf(str)) : new TeTpId(IetfInetUtil.ipAddressFor(str));
    }
}
