/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import java.util.Optional;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public interface DOMMountPointService extends DOMService<DOMMountPointService, DOMMountPointService.Extension> {
    /**
     * Marker interface for an extension to {@link DOMMountPointService}.
     */
    interface Extension extends DOMService.Extension<DOMMountPointService, Extension> {
        // Marker interface
    }

    Optional<DOMMountPoint> getMountPoint(YangInstanceIdentifier path);

    DOMMountPointBuilder createMountPoint(YangInstanceIdentifier path);

    Registration registerProvisionListener(DOMMountPointListener listener);

    interface DOMMountPointBuilder {

        <T extends DOMService<T, E>, E extends DOMService.Extension<T, E>> DOMMountPointBuilder addService(
            Class<T> type, T impl);

        // FIXME: just Registration
        ObjectRegistration<DOMMountPoint> register();
    }
}
