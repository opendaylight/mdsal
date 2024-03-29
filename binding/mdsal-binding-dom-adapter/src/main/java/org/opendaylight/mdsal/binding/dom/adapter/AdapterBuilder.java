/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;

public abstract class AdapterBuilder<T, D> {

    private final ClassToInstanceMap<D> delegates = MutableClassToInstanceMap.create();

    public abstract Set<? extends Class<? extends D>> getRequiredDelegates();

    protected abstract @NonNull T createInstance(@NonNull ClassToInstanceMap<D> immutableDelegates);

    private void checkAllRequiredServices() {
        for (final Class<? extends D> type : getRequiredDelegates()) {
            checkState(delegates.get(type) != null, "Requires service %s is not defined.", type);
        }
    }

    public final <V extends D> void addDelegate(final Class<V> type,final D impl) {
        delegates.put(type,impl);
    }

    /**
     * Check that all required {@code delegates} are present and return an instance of type {@code T}.
     *
     * @return Instance of {@code T}
     * @throws IllegalStateException if a required delegate instance is missing
     */
    public final @NonNull T build() {
        checkAllRequiredServices();
        return createInstance(ImmutableClassToInstanceMap.copyOf(delegates));
    }
}
