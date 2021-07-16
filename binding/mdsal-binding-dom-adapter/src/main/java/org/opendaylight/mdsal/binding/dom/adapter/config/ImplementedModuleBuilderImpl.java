/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.config;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import org.opendaylight.mdsal.binding.api.config.ConfigurationListener;
import org.opendaylight.mdsal.binding.api.config.ImplementationException;
import org.opendaylight.mdsal.binding.api.config.ImplementedModule;
import org.opendaylight.mdsal.binding.api.config.ImplementedModule.Builder;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataRoot;

final class ImplementedModuleBuilderImpl<M extends DataRoot> implements ImplementedModule.Builder<M> {
    private final Map<Class<? extends ChildOf<M>>, ConfigurationFragment<M, ?>> initialConfiguration = new HashMap<>();
    private final DefaultConfigurationService service;
    private final Class<M> module;

    ImplementedModuleBuilderImpl(final DefaultConfigurationService service, final Class<M> module) {
        this.service = requireNonNull(service);
        this.module = requireNonNull(module);
    }

    @Override
    public <T extends ChildOf<M>> Builder<M> addInitialConfiguration(final T configuration,
            final ConfigurationListener<T> listener) {
        final var fragment = new ConfigurationFragment<>(configuration, listener);
        final var iface = fragment.implementedInterface();
        final var existing = initialConfiguration.putIfAbsent(iface, fragment);
        if (existing != null) {
            throw new IllegalArgumentException("Configuration for " + iface + " already set to "
                + existing.initialConfiguration);
        }
        return this;
    }

    @Override
    public ImplementedModule<M> startImplementation(final Executor executor) throws ImplementationException {
        return service.startModule(module, initialConfiguration, executor);
    }
}
