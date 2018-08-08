/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.loader;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheLoader;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.builder.AdapterBuilder;

/**
 * Class for loading specific delegate type.
 *
 * @param <T>
 *            - built specific object type
 * @param <D>
 *            - delegates type
 */
@Beta
public abstract class AdapterLoader<T, D> extends CacheLoader<Class<? extends T>, Optional<T>> {

    @Nullable
    protected abstract D getDelegate(Class<? extends D> reqDeleg);

    @Nonnull
    protected abstract AdapterBuilder<? extends T, D> createBuilder(Class<? extends T> key);

    @Nonnull
    @Override
    public Optional<T> load(@Nonnull final Class<? extends T> key) {

        final AdapterBuilder<? extends T, D> builder = createBuilder(key);
        for (final Class<? extends D> reqDeleg : builder.getRequiredDelegates()) {
            final D deleg = getDelegate(reqDeleg);
            if (deleg != null) {
                builder.addDelegate(reqDeleg, deleg);
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(builder.build());
    }
}
