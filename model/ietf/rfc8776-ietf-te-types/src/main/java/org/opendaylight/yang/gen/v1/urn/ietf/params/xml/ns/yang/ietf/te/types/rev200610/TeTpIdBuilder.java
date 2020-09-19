/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.types.rev200610;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddressBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

public class TeTpIdBuilder {
    private TeTpIdBuilder() {
        // Hidden on purpose
    }

    public static TeTpId getDefaultInstance(final String defaultValue) {
        return defaultValue.indexOf('.') == -1 && defaultValue.indexOf(':') == -1
                ? new TeTpId(Uint32.valueOf(defaultValue))
                        : new TeTpId(IpAddressBuilder.getDefaultInstance(defaultValue));
    }
}
