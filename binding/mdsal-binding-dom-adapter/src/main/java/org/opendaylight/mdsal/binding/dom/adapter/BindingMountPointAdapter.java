/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BindingMountPointAdapter implements MountPoint {
    private static final class MountPointAdapterLoader extends BindingDOMAdapterLoader {
        private final DOMMountPoint domMountPoint;

        MountPointAdapterLoader(final BindingToNormalizedNodeCodec codec, final DOMMountPoint domMountPoint) {
            super(codec);
            this.domMountPoint = Preconditions.checkNotNull(domMountPoint);
        }

        @Override
        protected DOMService getDelegate(final Class<? extends DOMService> reqDeleg) {
            return domMountPoint.getService(reqDeleg).orNull();
        }
    }

    private static final class Services extends ClassValue<Optional<BindingService>> {
        private final BindingDOMAdapterLoader loader;

        Services(final BindingToNormalizedNodeCodec codec, final DOMMountPoint domMountPoint) {
            this.loader = new MountPointAdapterLoader(codec, domMountPoint);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Optional<BindingService> computeValue(final Class<?> type) {
            Verify.verify(BindingService.class.isAssignableFrom(type));
            return loader.load((Class<? extends BindingService>) type);
        }
    }

    private final InstanceIdentifier<?> identifier;
    private final Services services;

    public BindingMountPointAdapter(final BindingToNormalizedNodeCodec codec, final DOMMountPoint domMountPoint) {
        identifier = codec.getCodecRegistry().fromYangInstanceIdentifier(domMountPoint.getIdentifier());
        services = new Services(codec, domMountPoint);
    }

    @Override
    public InstanceIdentifier<?> getIdentifier() {
        return identifier;
    }

    @Override
    public <T extends BindingService> Optional<T> getService(final Class<T> service) {
        Optional<BindingService> potential = services.get(service);
        if(potential.isPresent()) {
            return Optional.of(service.cast(potential.get()));
        }
        return Optional.absent();
    }
}
