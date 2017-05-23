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
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;

/**
 * Interface implemented by classes interested in receiving notifications about
 * mount point changes.
 */
@Beta
public interface MountPointListener extends EventListener {

    /**
     * Invoked whenever mount point is created at given path represented by instance identifier.
     * @param path given path
     */
    void onMountPointCreated(InstanceIdentifier<?> path);

    /**
     * Invoked whenever mount point is removed from given path represented by instance identifier.
     * @param path given path
     */
    void onMountPointRemoved(InstanceIdentifier<?> path);
}
