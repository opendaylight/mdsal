/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.osgi;

import com.google.common.collect.Table;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.api.config.ConfigurationService;
import org.opendaylight.mdsal.binding.api.config.ImplementationException;
import org.opendaylight.mdsal.binding.api.config.ImplementedModule;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class BundleConfiguration extends AbstractRegistration {
    private static final Logger LOG = LoggerFactory.getLogger(BundleConfiguration.class);

    private final List<ImplementedModule<?>> implementedModules;
    private final BundleConfigurationExecutor executor;

    BundleConfiguration(final ConfigurationService configService, final Bundle bundle,
            // Consistency has already been checked
            final Table<Class<? extends DataRoot>, Class<? extends DataObject>, DataObject> modules) {
        executor = new BundleConfigurationExecutor(bundle);
        implementedModules = modules.rowMap().entrySet().stream()
            .map(entry -> {
                try {
                    return toImplementedModule(configService, entry.getKey(), entry.getValue());
                } catch (ImplementationException e) {
                    LOG.warn("Failed to start configuration of {} in bundle {}", entry.getKey(), bundle, e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableList());
    }

    @Override
    protected void removeRegistration() {
        implementedModules.forEach(ImplementedModule::close);
        executor.close();
    }

    private <M extends DataRoot, T extends ChildOf<M>> ImplementedModule<M> toImplementedModule(
            final ConfigurationService configService, final Class<? extends DataRoot> moduleClass,
            final Map<Class<? extends DataObject>, DataObject> children) throws ImplementationException {
        @SuppressWarnings("unchecked")
        final var builder = configService.implementModule((Class<M>) moduleClass);
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final var cast = Map.<Class<T>, T>copyOf((Map)children);
        for (var entry : cast.entrySet()) {
            builder.addInitialConfiguration(entry.getValue(),
                new ConfigurationComponentBridge<>(entry.getKey()));
        }

        return builder.startImplementation(executor);
    }
}
