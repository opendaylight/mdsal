/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.MountPointService.MountPointListener;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMMountPointListener;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.impl.codec.DeserializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class BindingDOMMountPointListenerAdapter implements Registration, DOMMountPointListener {
    private static final Logger LOG = LoggerFactory.getLogger(BindingDOMMountPointListenerAdapter.class);

    @VisibleForTesting
    final @NonNull MountPointListener listener;
    private final BindingDOMMountPointServiceAdapter service;
    private final Registration registration;

    BindingDOMMountPointListenerAdapter(final BindingDOMMountPointServiceAdapter service,
            final MountPointListener listener) {
        this.service = requireNonNull(service);
        this.listener = requireNonNull(listener);
        registration = service.getDelegate().registerProvisionListener(this);
    }

    @Override
    public void close() {
        registration.close();
    }

    @Override
    public void onMountPointCreated(final DOMMountPoint mountPoint) {
        final BindingMountPointAdapter binding;
        try {
            binding = service.getAdapter(mountPoint);
        } catch (UncheckedExecutionException e) {
            LOG.error("Unable to translate mountPoint {}. Omitting event.", mountPoint, e);
            return;
        }
        listener.onMountPointCreated(binding);
    }

    @Override
    public void onMountPointRemoved(final YangInstanceIdentifier path) {
        final DataObjectIdentifier<?> binding;
        try {
            binding = toBinding(path);
        } catch (final DeserializationException e) {
            LOG.error("Unable to translate mountPoint path {}. Omitting event.", path, e);
            return;
        }
        listener.onMountPointRemoved(binding);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("listener", listener).toString();
    }

    private @NonNull DataObjectIdentifier<?> toBinding(final YangInstanceIdentifier path)
            throws DeserializationException {
        final DataObjectReference<?> binding;
        try {
            binding = service.currentSerializer().fromYangInstanceIdentifier(path);
        } catch (IllegalArgumentException e) {
            throw new DeserializationException("Deserialization unsuccessful, " + path, e);
        }
        if (binding == null) {
            throw new DeserializationException("Deserialization unsuccessful, " + path);
        }
        try {
            return binding.toIdentifier();
        } catch (UnsupportedOperationException e) {
            throw new DeserializationException(e);
        }
    }
}