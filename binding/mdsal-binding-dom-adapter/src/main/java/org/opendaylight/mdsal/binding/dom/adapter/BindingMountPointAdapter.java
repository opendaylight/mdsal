/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import java.util.Optional;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class BindingMountPointAdapter implements MountPoint {
    private final InstanceIdentifier<?> identifier;
    private final LoadingCache<Class<? extends BindingService>, Optional<BindingService>> services;

    BindingMountPointAdapter(final AdapterContext codec, final DOMMountPoint domMountPoint) {
        identifier = codec.currentSerializer().fromYangInstanceIdentifier(domMountPoint.getIdentifier()).toLegacy();
        services = CacheBuilder.newBuilder().build(new BindingDOMAdapterLoader(codec) {
            @Override
            @SuppressWarnings({ "rawtypes", "unchecked" })
            protected DOMService<?, ?> getDelegate(final Class<? extends DOMService<?, ?>> reqDeleg) {
                return reqDeleg.cast(domMountPoint.getService((Class) reqDeleg).orElse(null));
            }
        });
    }

    @Override
    public InstanceIdentifier<?> getIdentifier() {
        return identifier;
    }

    @Override
    public <T extends BindingService> Optional<T> getService(final Class<T> service) {
        return services.getUnchecked(service).map(service::cast);
    }
}
