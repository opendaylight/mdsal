/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import java.util.Map;
import java.util.Optional;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(factory = OSGiMountPointService.FACTORY_NAME)
public final class OSGiMountPointService extends AbstractAdaptedService<MountPointService>
        implements MountPointService {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.binding.dom.adapter.osgi.OSGiMountPointService";

    @Activate
    public OSGiMountPointService(final Map<String, ?> properties) {
        super(MountPointService.class, properties);
    }

    @Deactivate
    void deactivate(final int reason) {
        stop(reason);
    }

    @Override
    public Optional<MountPoint> findMountPoint(final DataObjectIdentifier<?> path) {
        return delegate.findMountPoint(path);
    }

    @Override
    public Registration registerListener(final DataObjectReference<?> path, final MountPointListener listener) {
        return delegate.registerListener(path, listener);
    }
}
