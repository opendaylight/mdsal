/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.cache.CacheLoader;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public abstract class AdapterLoader<T, D> extends CacheLoader<Class<? extends T>, Optional<T>> {

    @Override
    public Optional<T> load(final Class<? extends T> key) {

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

    protected abstract @Nullable D getDelegate(Class<? extends D> reqDeleg);

    protected abstract @NonNull AdapterBuilder<? extends T, D> createBuilder(Class<? extends T> key);
}
