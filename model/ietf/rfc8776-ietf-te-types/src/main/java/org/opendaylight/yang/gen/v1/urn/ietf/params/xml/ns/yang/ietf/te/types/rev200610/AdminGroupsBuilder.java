/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.types.rev200610;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.HexString;

public class AdminGroupsBuilder {
    private AdminGroupsBuilder() {
        // Hidden on purpose
    }

    public static AdminGroups getDefaultInstance(final String defaultValue) {
        final HexString hex = new HexString(defaultValue);
        return defaultValue.length() <= 11 ? new AdminGroups(new AdminGroup(hex))
                : new AdminGroups(new ExtendedAdminGroup(hex));
    }
}
