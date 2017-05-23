/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import java.util.EventListener;
import java.util.Optional;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

@Beta
public interface MountPointService extends BindingService {

    Optional<MountPoint> getMountPoint(InstanceIdentifier<?> mountPoint);

    <T extends MountPointListener> ListenerRegistration<T> registerListener(InstanceIdentifier<?> path, T listener);


    interface MountPointListener extends EventListener {

        void onMountPointCreated(InstanceIdentifier<?> path);

        void onMountPointRemoved(InstanceIdentifier<?> path);

    }
}
