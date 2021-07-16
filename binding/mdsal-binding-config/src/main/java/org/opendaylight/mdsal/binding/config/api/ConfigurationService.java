/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.config.api;

import com.google.common.annotations.Beta;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataRoot;

/**
 * Consumer-side of configuration infrastructure. It allows registration to configuration object updates.
 */
@Beta
@NonNullByDefault
public interface ConfigurationService {
    /**
     * Register to receive values of a particular configuration object. Registered {@code listener} will be notified
     * every time a the corresponding object is update using supplied {@code executor}.
     *
     * @param <T> Configuration object type
     * @param type Configuration object class
     * @param listener Listener to be invoked
     * @param executor Executor to use when invoking the listener
     * @return A {@link Registration} handle
     * @throws NullPointerException if any argument is {@code null}
     */
    <T extends ChildOf<? super DataRoot>> Registration registerListener(Class<T> type,
        ConfigurationListener<T> listener, Executor executor);


    <M extends DataRoot> ImplementedModule.Builder<M> implementModule(Class<M> module);




}
