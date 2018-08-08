/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.mountpoint;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.api.BindingService;
import org.opendaylight.mdsal.binding.javav2.api.MountPoint;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.loader.BindingDOMAdapterLoader;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMService;

/**
 * Binding mount point adapter.
 */
@Beta
public class BindingMountPointAdapter implements MountPoint {

    private final InstanceIdentifier<?> identifier;
    private LoadingCache<Class<? extends BindingService>, Optional<BindingService>> services;

    public BindingMountPointAdapter(final BindingToNormalizedNodeCodec codec, final DOMMountPoint domMountPoint) {
        identifier = codec.getCodecRegistry().fromYangInstanceIdentifier(domMountPoint.getIdentifier());
        services = CacheBuilder.newBuilder().build(new BindingDOMAdapterLoader(codec) {

            @Nullable
            @Override
            protected DOMService getDelegate(final Class<? extends DOMService> reqDeleg) {
                return domMountPoint.getService(reqDeleg).orElse(null);
            }
        });
    }

    @Nonnull
    @Override
    public InstanceIdentifier<?> getIdentifier() {
        return identifier;
    }

    @Override
    public <T extends BindingService> Optional<T> getService(final Class<T> service) {
        final Optional<BindingService> potential = services.getUnchecked(service);
        if (potential.isPresent()) {
            return Optional.of(service.cast(potential.get()));
        }
        return Optional.empty();
    }
}
