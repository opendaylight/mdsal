/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.annotations.Beta;
import java.util.ServiceLoader;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecServices;

/**
 * A simple, constant implementation of the BlockingBindingNormalizer contract. This implementation is appropriate
 * in contexts where the backing context cannot change.
 */
@Beta
@MetaInfServices
@Singleton
public final class ConstantBlockingBindingNormalizer implements AdapterContext {
    private final @NonNull CachingBindingNormalizedNodeSerializer codec;

    public ConstantBlockingBindingNormalizer() {
        this(ServiceLoader.load(BindingDOMCodecServices.class).findFirst()
            .orElseThrow(() -> new IllegalStateException("Cannot load BindingDOMCodecServices")));
    }

    @Inject
    public ConstantBlockingBindingNormalizer(final BindingDOMCodecServices codec) {
        this.codec = new CachingBindingNormalizedNodeSerializer(codec);
    }

    @Override
    public CachingBindingNormalizedNodeSerializer currentSerializer() {
        return codec;
    }
}
