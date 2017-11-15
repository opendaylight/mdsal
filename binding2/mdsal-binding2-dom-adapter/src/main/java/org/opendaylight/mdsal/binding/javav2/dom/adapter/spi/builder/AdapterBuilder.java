/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.builder;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Builder;

/**
 * Class for building instances of delegates of specific type.
 *
 * @param <T>
 *            - builded specific object type
 * @param <D>
 *            - delegates type
 */
@Beta
public abstract class AdapterBuilder<T, D> implements Builder<T> {

    private final ClassToInstanceMap<D> delegates = MutableClassToInstanceMap.create();

    protected abstract T createInstance(ClassToInstanceMap<D> immutableDelegates);

    /**
     * Get required delegates.
     *
     * @return set of delegates
     */
    public abstract Set<? extends Class<? extends D>> getRequiredDelegates();

    /**
     * Add delegate to set of delegates.
     *
     * @param type
     *            - type of delegate
     * @param impl
     *            - implementation of delegate
     */
    public final <V extends D> void addDelegate(final Class<V> type, final D impl) {
        delegates.put(type, impl);
    }

    @Override
    public final T build() {
        checkAllRequiredServices();
        return createInstance(ImmutableClassToInstanceMap.copyOf(delegates));
    }

    private void checkAllRequiredServices() {
        for (final Class<? extends D> type : getRequiredDelegates()) {
            Preconditions.checkState(delegates.get(type) != null, "Requires service %s is not defined.", type);
        }
    }
}
