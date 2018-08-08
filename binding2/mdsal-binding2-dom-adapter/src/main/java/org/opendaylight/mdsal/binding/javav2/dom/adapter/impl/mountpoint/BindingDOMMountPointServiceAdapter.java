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
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.api.MountPoint;
import org.opendaylight.mdsal.binding.javav2.api.MountPointListener;
import org.opendaylight.mdsal.binding.javav2.api.MountPointService;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mount point service adapter.
 */
@Beta
public class BindingDOMMountPointServiceAdapter implements MountPointService {

    public static final Logger LOG = LoggerFactory.getLogger(BindingDOMMountPointServiceAdapter.class);

    private final BindingToNormalizedNodeCodec codec;
    private final DOMMountPointService mountService;
    private final LoadingCache<DOMMountPoint, BindingMountPointAdapter> bindingMountpoints =
            CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<DOMMountPoint, BindingMountPointAdapter>() {

                @Nonnull
                @Override
                public BindingMountPointAdapter load(@Nonnull final DOMMountPoint key) throws Exception {
                    return new BindingMountPointAdapter(codec, key);
                }
            });

    public BindingDOMMountPointServiceAdapter(final DOMMountPointService mountService,
            final BindingToNormalizedNodeCodec codec) {
        this.codec = codec;
        this.mountService = mountService;
    }

    @Override
    public Optional<MountPoint> getMountPoint(final InstanceIdentifier<?> mountPoint) {

        final YangInstanceIdentifier domPath = codec.toYangInstanceIdentifierBlocking(mountPoint);
        final Optional<DOMMountPoint> domMount = mountService.getMountPoint(domPath);
        if (domMount.isPresent()) {
            return Optional.ofNullable(bindingMountpoints.getUnchecked(domMount.get()));
        }
        return Optional.empty();
    }

    @Override
    public <T extends MountPointListener> ListenerRegistration<T> registerListener(final InstanceIdentifier<?> path,
            final T listener) {
        return new BindingDOMMountPointListenerAdapter<>(listener, codec, mountService);
    }

}
