/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.config;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.config.ConfigurationListener;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

// FIXME: Use a record when we have JDK17+
final class ConfigurationFragment<M extends DataRoot, T extends ChildOf<M>> implements Immutable {
    final @NonNull ConfigurationListener<T> listener;
    final @NonNull T initialConfiguration;

    ConfigurationFragment(final T configuration, final ConfigurationListener<T> listener) {
        this.initialConfiguration = requireNonNull(configuration);
        this.listener = requireNonNull(listener);
    }

    @SuppressWarnings("unchecked")
    @NonNull Class<T> implementedInterface() {
        return (Class<T>) initialConfiguration.implementedInterface();
    }

    ConfigurationListenerBridge<T> registerBridge(final DataBroker dataBroker) {
        return new ConfigurationListenerBridge<>(listener, dataBroker,
            InstanceIdentifier.create(implementedInterface()));
    }
}
