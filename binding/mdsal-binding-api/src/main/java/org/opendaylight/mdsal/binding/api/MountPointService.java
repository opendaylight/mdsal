/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.InstanceIdentifier;
import org.opendaylight.yangtools.concepts.Registration;

public interface MountPointService extends BindingService {

    Optional<MountPoint> getMountPoint(InstanceIdentifier<?> mountPoint);

    @NonNull Registration registerListener(InstanceIdentifier<?> path, MountPointListener listener);

    interface MountPointListener {

        void onMountPointCreated(@NonNull InstanceIdentifier<?> path);

        void onMountPointRemoved(@NonNull InstanceIdentifier<?> path);
    }
}
