/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import com.google.common.collect.ImmutableList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTransactionChain;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTreeChangePublisher;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDOMDataBroker extends AbstractDOMForwardedTransactionFactory<DOMStore>
        implements PingPongMergingDOMDataBroker {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDOMDataBroker.class);

    private final AtomicLong txNum = new AtomicLong();
    private final AtomicLong chainNum = new AtomicLong();
    private final @NonNull List<Extension> supportedExtensions;

    protected AbstractDOMDataBroker(final Map<LogicalDatastoreType, DOMStore> datastores) {
        super(datastores);

        final var builder = ImmutableList.<Extension>builder();
        if (isSupported(datastores, DOMStoreTreeChangePublisher.class)) {
            builder.add(new DataTreeChangeExtension() {
                @Override
                public Registration registerDataTreeListener(final DOMDataTreeIdentifier treeId,
                        final DOMDataTreeChangeListener listener) {
                    return getPublisher(treeId.datastore()).registerTreeChangeListener(treeId.path(), listener);
                }

                @Override
                @Deprecated(since = "13.0.0", forRemoval = true)
                public Registration registerLegacyDataTreeListener(final DOMDataTreeIdentifier treeId,
                        final DOMDataTreeChangeListener listener) {
                    return getPublisher(treeId.datastore()).registerLegacyTreeChangeListener(treeId.path(), listener);
                }

                private DOMStoreTreeChangePublisher getPublisher(final LogicalDatastoreType datastore) {
                    if (getTxFactories().get(datastore) instanceof DOMStoreTreeChangePublisher publisher) {
                        return publisher;
                    }
                    throw new IllegalStateException("Publisher for " + datastore + " data store is not available");
                }
            });
        }
        if (isSupported(datastores, CommitCohortExtension.class)) {
            builder.add((CommitCohortExtension) (path, cohort) -> {
                final var dsType = path.datastore();
                if (getTxFactories().get(dsType) instanceof CommitCohortExtension extension) {
                    return extension.registerCommitCohort(path, cohort);
                }
                throw new IllegalStateException("Cohort registry for " + dsType + " data store is not available");
            });
        }

        supportedExtensions = builder.build();
    }

    @Override
    public final List<Extension> supportedExtensions() {
        return supportedExtensions;
    }

    @Override
    public DOMTransactionChain createTransactionChain() {
        checkNotClosed();

        final var delegates = new EnumMap<LogicalDatastoreType, DOMStoreTransactionChain>(LogicalDatastoreType.class);
        for (var entry : getTxFactories().entrySet()) {
            delegates.put(entry.getKey(), entry.getValue().createTransactionChain());
        }

        final long chainId = chainNum.getAndIncrement();
        LOG.debug("Transactoin chain {} created, backing store chains {}", chainId, delegates);
        return new DOMDataBrokerTransactionChainImpl(chainId, delegates, this);
    }

    @Override
    protected final Object newTransactionIdentifier() {
        return "DOM-" + txNum.getAndIncrement();
    }

    private static boolean isSupported(final Map<LogicalDatastoreType, DOMStore> datastores,
            final Class<?> expDOMStoreInterface) {
        return datastores.values().stream().allMatch(expDOMStoreInterface::isInstance);
    }
}
