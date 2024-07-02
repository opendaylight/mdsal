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
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public interface MountPointService extends BindingService {

    Optional<MountPoint> findMountPoint(@NonNull DataObjectIdentifier<?> path);

    @Deprecated(since = "14.0.0", forRemoval = true)
    default Optional<MountPoint> getMountPoint(final InstanceIdentifier<?> mountPoint) {
        return findMountPoint(mountPoint.toIdentifier());
    }

    @NonNull Registration registerListener(DataObjectReference<?> path, MountPointListener listener);

    interface MountPointListener {
        // FIXME: pass down MountPoint itself
        void onMountPointCreated(@NonNull DataObjectIdentifier<?> path);

        void onMountPointRemoved(@NonNull DataObjectIdentifier<?> path);
    }
}
