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
import org.opendaylight.yangtools.concepts.Identifiable;

/**
 * A Node can be behind a mount point. In this case, the URI has to be in format
 * identifier/yang-ext:mount/identifier. The first identifier is the path to
 * a mount point and the second identifier is the path to a node behind the mount point.
 * A URI can end in a mount point itself by using identifier/yang-ext:mount.
 */
@Beta
public interface MountPoint extends Identifiable<InstanceIdentifier<?>> {

    /**
     * Based on given service class, it returns binding service (from cache).
     * @param service service class
     * @param <T> service type
     * @return optional of binding service
     */
    <T extends BindingService> Optional<T> getService(Class<T> service);
}
