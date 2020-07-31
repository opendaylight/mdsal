/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import com.google.common.annotations.Beta;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.MountPointService.MountPointListener;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Beta
@Component(factory = OSGiMountPointService.FACTORY_NAME)
public final class OSGiMountPointService extends AbstractAdaptedService<MountPointService>
        implements MountPointService {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.binding.dom.adapter.osgi.OSGiMountPointService";

    public OSGiMountPointService() {
        super(MountPointService.class);
    }

    @Override
    public Optional<MountPoint> getMountPoint(final InstanceIdentifier<?> mountPoint) {
        return delegate().getMountPoint(mountPoint);
    }

    @Override
    public <T extends MountPointListener> ListenerRegistration<T> registerListener(final InstanceIdentifier<?> path,
            final T listener) {
        return delegate().registerListener(path, listener);
    }

    @Activate
    void activate(final Map<String, ?> properties) {
        start(properties);
    }

    @Deactivate
    void deactivate(final int reason) {
        stop(reason);
    }
}
