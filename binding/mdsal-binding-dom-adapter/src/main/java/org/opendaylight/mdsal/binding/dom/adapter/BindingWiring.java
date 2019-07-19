/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.dom.adapter.spi.AdapterFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;

/**
 *
 * @author Yi Yang
 */
@Beta
@NonNullByDefault
@Singleton
public final class BindingWiring {
    private final AdapterFactory adapterFactory;
    private final BindingCodecTreeFactory bindingCodecTreeFactory;
    private final BindingNormalizedNodeSerializer bindingNormalizedNodeSerializer;

    @Inject
    public BindingWiring(final AdapterFactory adapterFactory, final BindingCodecTreeFactory bindingCodecTreeFactory, final BindingNormalizedNodeSerializer bindingNormalizedNodeSerializer) {
        this.adapterFactory = requireNonNull(adapterFactory);
        this.bindingCodecTreeFactory = requireNonNull(bindingCodecTreeFactory);
        this.bindingNormalizedNodeSerializer = requireNonNull(bindingNormalizedNodeSerializer);
    }

    @Singleton AdapterFactory getAdapterFactory() {
        return this.adapterFactory;
    }


    @Singleton BindingNormalizedNodeSerializer getBindingNormalizedNodeSerializer() {
        return this.bindingNormalizedNodeSerializer;
    }

    @Singleton BindingCodecTreeFactory getBindingCodecTreeFactory() {
        return this.bindingCodecTreeFactory;
    }
}
