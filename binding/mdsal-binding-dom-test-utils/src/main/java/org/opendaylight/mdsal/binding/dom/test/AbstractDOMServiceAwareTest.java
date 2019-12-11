/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.test;

import com.google.common.annotations.Beta;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.After;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.mdsal.dom.broker.DOMNotificationRouter;
import org.opendaylight.mdsal.dom.broker.DOMRpcRouter;
import org.opendaylight.mdsal.dom.broker.SerializedDOMDataBroker;
import org.opendaylight.mdsal.dom.spi.FixedDOMSchemaService;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStore;

/**
 * Base class for tests running with models packaged as Binding artifacts on their classpath and need to interact with
 * {@link DOMService}s. Services are lazily instantiated on-demand.
 *
 * @author Robert Varga
 */
@Beta
public class AbstractDOMServiceAwareTest extends AbstractSchemaAwareTest {
    private static final @NonNull ImmutableSet<LogicalDatastoreType> BOTH_DATASTORES = ImmutableSet.of(
        LogicalDatastoreType.CONFIGURATION,
        LogicalDatastoreType.OPERATIONAL);

    private volatile DOMNotificationRouter domNotificationRouter;
    private volatile TestingDOMDataBroker domDataBroker;
    private volatile DOMSchemaService domSchemaService;
    private volatile DOMRpcRouter domRpcRouter;

    @After
    public final synchronized void teardownDOMServices() {
        if (domDataBroker != null) {
            domDataBroker.close();
        }
    }

    protected final DOMActionProviderService domActionProviderService() {
        return rpcRouter().getActionProviderService();
    }

    protected final DOMActionService domActionService() {
        return rpcRouter().getActionService();
    }

    protected final DOMDataBroker domDataBroker() {
        TestingDOMDataBroker local = domDataBroker;
        if (local == null) {
            synchronized (this) {
                local = domDataBroker;
                if (local == null) {
                    domDataBroker = local = createDomDataBroker();
                }
            }
        }
        return local;
    }

    protected final DOMRpcProviderService domRpcProviderService() {
        return rpcRouter().getRpcProviderService();
    }

    protected final DOMRpcService domRpcService() {
        return rpcRouter().getRpcService();
    }

    protected final DOMSchemaService domSchemaService() {
        DOMSchemaService local = domSchemaService;
        if (local == null) {
            synchronized (this) {
                local = domSchemaService;
                if (local == null) {
                    domSchemaService = local = FixedDOMSchemaService.of(() -> {
                        try {
                            return effectiveModelContext();
                        } catch (Exception e) {
                            Throwables.throwIfUnchecked(e);
                            throw new LinkageError("Failed to assemble effective model context", e);
                        }
                    });
                }
            }
        }
        return local;
    }

    protected @NonNull Set<LogicalDatastoreType> testedDatastoreTypes() {
        return BOTH_DATASTORES;
    }

    private @NonNull TestingDOMDataBroker createDomDataBroker() {
        final Map<LogicalDatastoreType, DOMStore> datastores = new HashMap<>();
        for (LogicalDatastoreType type : testedDatastoreTypes()) {
            final InMemoryDOMDataStore store = new InMemoryDOMDataStore(type.toString(),
                // Default to concurrency?
                Executors.newSingleThreadExecutor());
            store.onGlobalContextUpdated(effectiveModelContext());
            datastores.put(type, store);
        }
        return new TestingDOMDataBroker(datastores);
    }

    private DOMRpcRouter rpcRouter() {
        DOMRpcRouter local = domRpcRouter;
        if (local == null) {
            synchronized (this) {
                local = domRpcRouter;
                if (local == null) {
                    domRpcRouter = local = DOMRpcRouter.newInstance(domSchemaService());
                }
            }
        }
        return local;
    }

    protected class TestingDOMDataBroker extends SerializedDOMDataBroker {
        protected TestingDOMDataBroker(final Map<LogicalDatastoreType, DOMStore> datastores) {
            // We always want to use direct execution of datastore operations
            super(datastores, MoreExecutors.newDirectExecutorService());
        }
    }
}
