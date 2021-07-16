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
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataRoot;

/**
 * @author nite
 *
 */
@Beta
public interface ImplementedModule<M extends DataRoot> extends Registration {

    public interface Builder<M extends DataRoot> extends Mutable {
        Builder<M> withExecutor();

        <T extends ChildOf<M>> Builder<M> addInitialConfiguration(T configuration, ConfigurationListener<T> listener);


        /**
         * Start the proposed implementation using specified executor.
         *
         * @param executor
         * @return Implementation handle
         * @throws NullPointerException if {@code executor} is null
         * @throws ImplementationException if the proposed implementation cannot be started
         */
        ImplementedModule<M> startImplementation(Executor executor) throws ImplementationException;
    }
}
