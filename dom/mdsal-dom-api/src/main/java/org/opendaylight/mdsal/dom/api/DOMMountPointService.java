/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.api;

import java.util.Optional;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public interface DOMMountPointService extends DOMService {

    Optional<DOMMountPoint> getMountPoint(YangInstanceIdentifier path);

    DOMMountPointBuilder createMountPoint(YangInstanceIdentifier path);

    ListenerRegistration<DOMMountPointListener> registerProvisionListener(DOMMountPointListener listener);

    interface DOMMountPointBuilder {

        <T extends DOMService> DOMMountPointBuilder addService(Class<T> type,T impl);

        ObjectRegistration<DOMMountPoint> register();
    }
}
