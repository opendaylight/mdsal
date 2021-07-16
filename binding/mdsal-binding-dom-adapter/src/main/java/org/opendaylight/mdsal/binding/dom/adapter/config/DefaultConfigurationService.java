/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.config;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.config.ConfigurationService;
import org.opendaylight.mdsal.binding.api.config.ImplementationException;
import org.opendaylight.mdsal.binding.api.config.ImplementedModule;
import org.opendaylight.mdsal.binding.api.config.ImplementedModule.Builder;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Singleton
@Component
public final class DefaultConfigurationService implements ConfigurationService {
    private final DataBroker dataBroker;

    @Inject
    @Activate
    public DefaultConfigurationService(@Reference final DataBroker dataBroker) {
        this.dataBroker = requireNonNull(dataBroker);
    }

    @Override
    public <M extends @NonNull DataRoot> @NonNull Builder<M> implementModule(final Class<M> module) {
        return new ImplementedModuleBuilderImpl<>(this, module);
    }

    <M extends DataRoot> @NonNull ImplementedModule<M> startModule(final Class<M> module,
            final Map<Class<? extends ChildOf<M>>, ConfigurationFragment<M, ?>> fragments,
            final @NonNull Executor executor) throws ImplementationException {
        if (fragments.isEmpty()) {
            throw new ImplementationException("Module implementation requires at least one initial configuration");
        }

        // Create registered bridges
        final var bridges = fragments.values().stream()
            .map(fragment -> fragment.registerBridge(dataBroker))
            .collect(Collectors.toUnmodifiableList());

        // FIXME: populate the datastore

        // Enable configuration updates
        bridges.forEach(bridge -> bridge.enable(executor));

        return new ImplementedModuleImpl<>(bridges);
    }
}
