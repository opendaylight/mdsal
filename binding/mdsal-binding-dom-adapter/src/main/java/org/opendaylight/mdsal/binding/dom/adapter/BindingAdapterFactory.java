/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.ServiceLoader;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.dom.adapter.spi.AdapterFactory;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Implementation of AdapterFactory.
 *
 * @author Robert Varga
 */
@Beta
@MetaInfServices(value = AdapterFactory.class)
@NonNullByDefault
@Singleton
public final class BindingAdapterFactory extends AbstractAdapterFactory implements Immutable {
    private final AdapterContext codec;

    public BindingAdapterFactory() {
        this(ServiceLoader.load(AdapterContext.class).findFirst()
            .orElseThrow(() -> new IllegalStateException("Failed to load BlockingBindingNormalizer")));
    }

    @Inject
    public BindingAdapterFactory(final AdapterContext codec) {
        this.codec = requireNonNull(codec);
    }

    @Override
    AdapterContext codec() {
        return codec;
    }
}
