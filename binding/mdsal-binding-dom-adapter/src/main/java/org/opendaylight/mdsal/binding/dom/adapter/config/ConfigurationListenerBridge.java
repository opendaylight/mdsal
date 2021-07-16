/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.config;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;

import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.config.ConfigurationListener;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class ConfigurationListenerBridge<T extends DataObject> extends AbstractRegistration
        implements ClusteredDataTreeChangeListener<T> {
    private final ConfigurationListener<T> listener;
    private final Registration reg;

    private volatile Executor executor;

    ConfigurationListenerBridge(final ConfigurationListener<T> listener, final DataBroker dataBroker,
            final @NonNull InstanceIdentifier<T> path) {
        this.listener = requireNonNull(listener);
        reg = dataBroker.registerDataTreeChangeListener(DataTreeIdentifier.create(CONFIGURATION, path), this);
    }

    @Override
    public void onDataTreeChanged(final Collection<DataTreeModification<T>> changes) {
        final var config = Iterables.getLast(changes).getRootNode().getDataAfter();
        final var exec = executor;
        if (exec != null) {
            exec.execute(() -> listener.onConfiguration(config));
        }
    }

    @Override
    protected void removeRegistration() {
        reg.close();
    }

    void enable(final @NonNull Executor newWxecutor) {
        this.executor = requireNonNull(newExecutor);
    }
}
