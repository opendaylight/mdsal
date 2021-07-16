/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.config;

import com.google.common.annotations.Beta;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataRoot;

/**
 * An implemented module, bound to the configuration datastore. It remains operational as long as it is not
 * {@link #close()}d.
 *
 * @param <M> Module type
 */
@Beta
public interface ImplementedModule<M extends DataRoot> extends Registration {

    /**
     * Builder for {@link ImplementedModule}. It should be used fluently to add one or more initial configuration
     * items and corresponding listeners via {@link #addInitialConfiguration(ChildOf, ConfigurationListener)} after
     * which it needs to be started via {@link #startImplementation(Executor)}.
     *
     * @param <M> Module type
     */
    public interface Builder<M extends DataRoot> extends Mutable {
        /**
         * Add an initial configuration fragment corresponding to a top-level element of a module.
         *
         * @param <T> DataObject type
         * @param configuration Initial configuration in case there is no existing configuration
         * @param listener Listener to invoke when the configuration has been established or updated
         * @return This builder
         * @throws NullPointerException if any argument is null
         * @throws IllegalArgumentException if the configuration has already been added
         */
        <T extends ChildOf<M>> @NonNull Builder<M> addInitialConfiguration(T configuration,
            ConfigurationListener<T> listener);

        /**
         * Start the proposed implementation using specified executor.
         *
         * @param executor Executor to use for dispatching updates
         * @return Implementation handle
         * @throws NullPointerException if {@code executor} is null
         * @throws ImplementationException if the proposed implementation cannot be started
         */
        @NonNull ImplementedModule<M> startImplementation(Executor executor) throws ImplementationException;
    }
}
