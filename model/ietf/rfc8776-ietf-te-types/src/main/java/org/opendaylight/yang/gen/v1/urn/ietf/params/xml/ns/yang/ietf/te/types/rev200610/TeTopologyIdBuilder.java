/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.types.rev200610;

public class TeTopologyIdBuilder {
    private static final TeTopologyId EMPTY = new TeTopologyId("");

    private TeTopologyIdBuilder() {
        // Hidden on purpose
    }

    public static TeTopologyId getDefaultInstance(final String defaultValue) {
        return defaultValue.isEmpty() ? EMPTY : new TeTopologyId(defaultValue);
    }
}
