/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.MutableClassToInstanceMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Singleton;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMMountPointListener;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.mdsal.dom.spi.SimpleDOMMountPoint;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.util.ListenerRegistry;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
@MetaInfServices
@Singleton
public final class DOMMountPointServiceImpl implements DOMMountPointService {
    private static final Logger LOG = LoggerFactory.getLogger(DOMMountPointServiceImpl.class);

    private final Map<YangInstanceIdentifier, DOMMountPoint> mountPoints = new HashMap<>();

    private final ListenerRegistry<DOMMountPointListener> listeners = ListenerRegistry.create();

    @Override
    public Optional<DOMMountPoint> getMountPoint(final YangInstanceIdentifier path) {
        return Optional.ofNullable(mountPoints.get(path));
    }

    @Override
    public synchronized DOMMountPointBuilder createMountPoint(final YangInstanceIdentifier path) {
        checkNotExists(path, mountPoints.get(requireNonNull(path)));
        return new DOMMountPointBuilderImpl(path);
    }

    @Override
    public ListenerRegistration<DOMMountPointListener> registerProvisionListener(final DOMMountPointListener listener) {
        return listeners.register(listener);
    }

    @Activate
    @SuppressWarnings("static-method")
    void activate() {
        LOG.info("DOMMountPointService activated");
    }

    @Deactivate
    @SuppressWarnings("static-method")
    void deactivate() {
        LOG.info("DOMMountPointService deactivated");
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private ObjectRegistration<DOMMountPoint> registerMountPoint(final SimpleDOMMountPoint mountPoint) {
        final YangInstanceIdentifier mountPointId = mountPoint.getIdentifier();
        synchronized (mountPoints) {
            checkNotExists(mountPointId, mountPoints.putIfAbsent(mountPointId, mountPoint));
        }
        listeners.streamListeners().forEach(listener -> {
            try {
                listener.onMountPointCreated(mountPointId);
            } catch (final Exception ex) {
                LOG.error("Listener {} failed on mount point {} created event", listener, mountPoint, ex);
            }
        });

        return new AbstractObjectRegistration<>(mountPoint) {
            @Override
            protected void removeRegistration() {
                unregisterMountPoint(getInstance().getIdentifier());
            }
        };
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void unregisterMountPoint(final YangInstanceIdentifier mountPointId) {
        synchronized (mountPoints) {
            if (mountPoints.remove(mountPointId) == null) {
                LOG.warn("Removing non-existent mount point {} at", mountPointId, new Throwable());
                return;
            }
        }

        listeners.streamListeners().forEach(listener -> {
            try {
                listener.onMountPointRemoved(mountPointId);
            } catch (final Exception ex) {
                LOG.error("Listener {} failed on mount point {} removed event", listener, mountPointId, ex);
            }
        });
    }

    private static void checkNotExists(final YangInstanceIdentifier id, final DOMMountPoint mountPoint) {
        checkState(mountPoint == null, "Mount point %s already exists as %s", id, mountPoint);
    }

    private final class DOMMountPointBuilderImpl implements DOMMountPointBuilder {
        private final MutableClassToInstanceMap<DOMService> services = MutableClassToInstanceMap.create();
        private final YangInstanceIdentifier path;

        private SimpleDOMMountPoint mountPoint;

        DOMMountPointBuilderImpl(final YangInstanceIdentifier path) {
            this.path = requireNonNull(path);
        }

        @Override
        public <T extends DOMService> DOMMountPointBuilder addService(final Class<T> type, final T impl) {
            services.putInstance(requireNonNull(type), requireNonNull(impl));
            return this;
        }

        @Override
        public ObjectRegistration<DOMMountPoint> register() {
            checkState(mountPoint == null, "Mount point is already built.");
            mountPoint = SimpleDOMMountPoint.create(path, services);
            return registerMountPoint(mountPoint);
        }
    }
}
