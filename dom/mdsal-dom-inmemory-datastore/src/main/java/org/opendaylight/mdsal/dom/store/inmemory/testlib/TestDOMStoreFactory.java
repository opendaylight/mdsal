/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory.testlib;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutorService;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStore;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStoreConfigProperties;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * A factory for instantiating {@link InMemoryDOMDataStore} instances for testing purposes.
 *
 * @since 17.0.0
 */
@Beta
public final class TestDOMStoreFactory {
    /**
     * A builder of {@link TestDOMStoreFactory} instances.
     */
    public static final class Builder {
        @NonNullByDefault
        private final DataTreeFactory dataTreeFactory;

        private boolean debugTransactions;

        Builder(final DataTreeFactory dataTreeFactory) {
            this.dataTreeFactory = requireNonNull(dataTreeFactory);
        }

        public Builder setDebugTransactions(final boolean debugTransactions) {
            this.debugTransactions = debugTransactions;
            return this;
        }

        @NonNullByDefault
        public TestDOMStoreFactory build() {
            return new TestDOMStoreFactory(dataTreeFactory, debugTransactions);
        }
    }

    @NonNullByDefault
    private final DataTreeFactory dataTreeFactory;
    private final boolean debugTransactions;

    @NonNullByDefault
    private TestDOMStoreFactory(final DataTreeFactory dataTreeFactory, final boolean debugTransactions) {
        this.dataTreeFactory = requireNonNull(dataTreeFactory);
        this.debugTransactions = debugTransactions;
    }

    @NonNullByDefault
    private InMemoryDOMDataStore newDOMStore(final String name, final DataTreeConfiguration config,
            final ExecutorService dataChangeListenerExecutor) {
        return new InMemoryDOMDataStore(name, dataTreeFactory, config, dataChangeListenerExecutor,
            InMemoryDOMDataStoreConfigProperties.DEFAULT_MAX_DATA_CHANGE_LISTENER_QUEUE_SIZE, debugTransactions);
    }

    @NonNullByDefault
    public InMemoryDOMDataStore newDOMStore(final String name, final DataTreeConfiguration config,
            final EffectiveModelContext modelContext, final ExecutorService dataChangeListenerExecutor) {
        final var ret = newDOMStore(name, config, dataChangeListenerExecutor);
        ret.onModelContextUpdated(modelContext);
        return ret;
    }

    @NonNullByDefault
    public InMemoryDOMDataStore newDOMStore(final String name, final DataTreeConfiguration config,
            final DOMSchemaService schemaService, final ExecutorService dataChangeListenerExecutor) {
        final var ret = newDOMStore(name, config, dataChangeListenerExecutor);
        schemaService.registerSchemaContextListener(ret::onModelContextUpdated);
        return ret;
    }

    @NonNullByDefault
    public InMemoryDOMDataStore newDirectDOMStore(final String name, final DataTreeConfiguration config,
            final EffectiveModelContext modelContext) {
        return newDOMStore(requireNonNull(name), requireNonNull(config), requireNonNull(modelContext),
            MoreExecutors.newDirectExecutorService());
    }

    @NonNullByDefault
    public InMemoryDOMDataStore newDirectDOMStore(final String name, final DataTreeConfiguration config,
            final DOMSchemaService schemaService) {
        return newDOMStore(requireNonNull(name), requireNonNull(config), requireNonNull(schemaService),
            MoreExecutors.newDirectExecutorService());
    }

    @NonNullByDefault
    public static Builder builder(final DataTreeFactory dataTreeFactory) {
        return new Builder(dataTreeFactory);
    }
}
