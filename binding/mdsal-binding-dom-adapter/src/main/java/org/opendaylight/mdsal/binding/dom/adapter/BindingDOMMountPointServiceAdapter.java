/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import java.util.Optional;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;

public class BindingDOMMountPointServiceAdapter
        extends AbstractBindingLoadingAdapter<DOMMountPointService, DOMMountPoint, BindingMountPointAdapter>
        implements MountPointService {
    public BindingDOMMountPointServiceAdapter(final AdapterContext adapterContext,
            final DOMMountPointService mountService) {
        super(adapterContext, mountService);
    }

    @Override
    public Optional<MountPoint> findMountPoint(final DataObjectIdentifier<?> path) {
        final var domPath = currentSerializer().toCachedYangInstanceIdentifier(path);
        return getDelegate().getMountPoint(domPath).map(this::getAdapter);
    }

    @Override
    public Registration registerListener(final DataObjectReference<?> path, final MountPointListener listener) {
        return new BindingDOMMountPointListenerAdapter(this, listener);
    }

    @Override
    BindingMountPointAdapter loadAdapter(final DOMMountPoint key) {
        return new BindingMountPointAdapter(adapterContext(), key);
    }
}
