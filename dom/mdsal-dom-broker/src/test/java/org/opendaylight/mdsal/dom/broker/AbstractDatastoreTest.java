/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ForwardingExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStore;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStoreConfigProperties;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeFactory;
import org.opendaylight.yangtools.yang.data.tree.dagger.ReferenceDataTreeFactoryModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

public abstract class AbstractDatastoreTest {
    static final class CommitExecutorService extends ForwardingExecutorService {
        // FIXME: remove external users
        ExecutorService delegate;

        CommitExecutorService() {
            delegate = Executors.newSingleThreadExecutor();
        }

        CommitExecutorService(final ExecutorService delegate) {
            this.delegate = requireNonNull(delegate);
        }

        @Override
        protected ExecutorService delegate() {
            return delegate;
        }
    }

    private static EffectiveModelContext MODEL_CONTEXT;
    private static DataTreeFactory DATA_TREE_FACTORY;

    @BeforeClass
    public static final void beforeClass() {
        MODEL_CONTEXT = TestModel.createTestContext();
        DATA_TREE_FACTORY = ReferenceDataTreeFactoryModule.provideDataTreeFactory();
    }

    @AfterClass
    public static final void afterClass() {
        MODEL_CONTEXT = null;
        DATA_TREE_FACTORY = null;
    }

    static final @NonNull InMemoryDOMDataStore newDOMStore(final LogicalDatastoreType type) {
        final String name;
        final DataTreeConfiguration config;
        switch (type) {
            case null -> throw new NullPointerException();
            case CONFIGURATION -> {
                name = "CONFIG";
                config = DataTreeConfiguration.DEFAULT_CONFIGURATION;
            }
            case OPERATIONAL -> {
                name = "OPER";
                config = DataTreeConfiguration.DEFAULT_OPERATIONAL;
            }
        }

        final var store = new InMemoryDOMDataStore(name, DATA_TREE_FACTORY, config,
            MoreExecutors.newDirectExecutorService(),
            InMemoryDOMDataStoreConfigProperties.DEFAULT_MAX_DATA_CHANGE_LISTENER_QUEUE_SIZE, false);
        store.onModelContextUpdated(MODEL_CONTEXT);
        return store;
    }
}
