/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

/**
 *  A {@link BindingService} providing access to mount point.
 */
@Beta
public interface MountPointService extends BindingService {

    /**
     * Returns optional of mount point at given path represented by instance identifier.
     * @param mountPoint mount point instance identifier
     * @return optional of mount point
     */
    Optional<MountPoint> getMountPoint(InstanceIdentifier<?> mountPoint);

    /**
     * Register a {@link MountPointListener} instance. Once registered, the listener will start
     * receiving changes on the selected path.
     *
     * @param path given path to listen to changes
     * @param listener {@link MountPointListener} that is being registered
     * @param <T> listener type
     * @return Listener registration object, which may be used to unregister
     *         your listener using {@link ListenerRegistration#close()} to stop
     *         delivery of mount point change events.
     */
    <T extends MountPointListener> ListenerRegistration<T> registerListener(InstanceIdentifier<?> path, T listener);
}
