/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.model.ietf.util;

final class MacUtil extends AbstractIetfYangUtil<MacClass, PhysClass> {
    MacUtil() {
        super(MacClass.class, PhysClass.class);
    }

    @Override
    protected String getValue(final MacClass macAddress) {
        return macAddress.getValue();
    }

    @Override
    protected String getPhysValue(PhysClass physAddress) {
        return physAddress.getValue();
    }
}