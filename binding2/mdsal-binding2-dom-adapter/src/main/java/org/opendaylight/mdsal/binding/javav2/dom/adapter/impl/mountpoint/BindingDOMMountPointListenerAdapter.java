/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.mountpoint;

import com.google.common.annotations.Beta;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.api.MountPointListener;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.dom.api.DOMMountPointListener;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.impl.codec.DeserializationException;

/**
 * Mount point listener adapter.
 *
 * @param <T>
 *            - {@link MountPointListener} type
 */
@Beta
public final class BindingDOMMountPointListenerAdapter<T extends MountPointListener>
        implements ListenerRegistration<T>, DOMMountPointListener {

    private final T listener;
    private final ListenerRegistration<DOMMountPointListener> registration;
    private final BindingToNormalizedNodeCodec codec;

    public BindingDOMMountPointListenerAdapter(final T listener, final BindingToNormalizedNodeCodec codec,
            final DOMMountPointService mountService) {
        this.listener = listener;
        this.codec = codec;
        this.registration = mountService.registerProvisionListener(this);
    }

    @Nonnull
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
            final InstanceIdentifier<? extends TreeNode> bindingPath = toBinding(path);
            listener.onMountPointCreated(bindingPath);
        } catch (final DeserializationException e) {
            BindingDOMMountPointServiceAdapter.LOG.error("Unable to translate mountPoint path {}." + " Omitting event.",
                    path, e);
        }
    }

    private InstanceIdentifier<? extends TreeNode> toBinding(final YangInstanceIdentifier path)
            throws DeserializationException {
        final Optional<InstanceIdentifier<? extends TreeNode>> instanceIdentifierOptional = codec.toBinding(path);
        if (instanceIdentifierOptional.isPresent()) {
            return instanceIdentifierOptional.get();
        } else {
            throw new DeserializationException("Deserialization unsuccessful, " + instanceIdentifierOptional);
        }
    }

    @Override
    public void onMountPointRemoved(final YangInstanceIdentifier path) {
        try {
            final InstanceIdentifier<? extends TreeNode> bindingPath = toBinding(path);
            listener.onMountPointRemoved(bindingPath);
        } catch (final DeserializationException e) {
            BindingDOMMountPointServiceAdapter.LOG.error("Unable to translate mountPoint path {}." + " Omitting event.",
                    path, e);
        }
    }
}
