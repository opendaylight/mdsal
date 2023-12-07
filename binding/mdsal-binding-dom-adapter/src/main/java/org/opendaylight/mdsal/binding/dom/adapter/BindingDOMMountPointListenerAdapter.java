/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.MountPointService.MountPointListener;
import org.opendaylight.mdsal.dom.api.DOMMountPointListener;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.impl.codec.DeserializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class BindingDOMMountPointListenerAdapter<T extends MountPointListener> implements
            ListenerRegistration<T>, DOMMountPointListener {
    private static final Logger LOG = LoggerFactory.getLogger(BindingDOMMountPointListenerAdapter.class);

    private final @NonNull T listener;
    private final AdapterContext adapterContext;
    private final Registration registration;

    BindingDOMMountPointListenerAdapter(final T listener, final AdapterContext adapterContext,
            final DOMMountPointService mountService) {
        this.listener = requireNonNull(listener);
        this.adapterContext = requireNonNull(adapterContext);
        registration = mountService.registerProvisionListener(this);
    }

    @Override
    public T getInstance() {
        return listener;
    }

    @Override
    public void close() {
        registration.close();
    }

    @Override
    public void onMountPointCreated(final YangInstanceIdentifier path) {
        try {
            listener.onMountPointCreated(toBinding(path));
        } catch (final DeserializationException e) {
            LOG.error("Unable to translate mountPoint path {}. Omitting event.", path, e);
        }
    }

    @Override
    public void onMountPointRemoved(final YangInstanceIdentifier path) {
        try {
            listener.onMountPointRemoved(toBinding(path));
        } catch (final DeserializationException e) {
            LOG.error("Unable to translate mountPoint path {}. Omitting event.", path, e);
        }
    }

    private InstanceIdentifier<? extends DataObject> toBinding(final YangInstanceIdentifier path)
            throws DeserializationException {
        final InstanceIdentifier<?> binding;
        try {
            binding = adapterContext.currentSerializer().fromYangInstanceIdentifier(path);
        } catch (IllegalArgumentException e) {
            throw new DeserializationException("Deserialization unsuccessful, " + path, e);
        }
        if (binding == null) {
            throw new DeserializationException("Deserialization unsuccessful, " + path);
        }
        return binding;
    }


}