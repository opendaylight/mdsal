/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.base.Optional;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingDOMMountPointServiceAdapter
        extends AbstractBindingLoadingAdapter<DOMMountPointService, DOMMountPoint, BindingMountPointAdapter>
        implements MountPointService {
    // FIXME: this really should be hidden
    static final Logger LOG = LoggerFactory.getLogger(BindingDOMMountPointServiceAdapter.class);

    public BindingDOMMountPointServiceAdapter(final DOMMountPointService mountService,
            final BindingToNormalizedNodeCodec codec) {
        super(codec, mountService);
    }

    @Override
    public Optional<MountPoint> getMountPoint(final InstanceIdentifier<?> mountPoint) {
        YangInstanceIdentifier domPath = getCodec().toYangInstanceIdentifierBlocking(mountPoint);
        Optional<DOMMountPoint> domMount = getDelegate().getMountPoint(domPath);
        return domMount.transform(this::getAdapter);
    }

    @Override
    public <T extends MountPointListener> ListenerRegistration<T> registerListener(final InstanceIdentifier<?> path,
            final T listener) {
        return new BindingDOMMountPointListenerAdapter<>(listener, getCodec(), getDelegate());
    }

    @Override
    BindingMountPointAdapter loadAdapter(final DOMMountPoint key) {
        return new BindingMountPointAdapter(getCodec(), key);
    }
}
