/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionChainListener;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataBrokerExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.dom.api.xpath.DOMDataBrokerTransactionXPathSupport;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreReadTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTransactionChain;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTreeChangePublisher;
import org.opendaylight.mdsal.dom.spi.store.XPathAwareDOMStore;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDOMDataBroker extends AbstractDOMForwardedTransactionFactory<DOMStore>
        implements DOMDataBroker, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDOMDataBroker.class);

    private final AtomicLong txNum = new AtomicLong();
    private final AtomicLong chainNum = new AtomicLong();
    private final Map<Class<? extends DOMDataBrokerExtension>, DOMDataBrokerExtension> extensions;
    private volatile AutoCloseable closeable;

    protected AbstractDOMDataBroker(final Map<LogicalDatastoreType, DOMStore> datastores) {
        super(datastores);

        final Builder<Class<? extends DOMDataBrokerExtension>, DOMDataBrokerExtension> b = ImmutableMap.builder();

        if (datastores.values().stream().allMatch(DOMStoreTreeChangePublisher.class::isInstance)) {
            b.put(DOMDataTreeChangeService.class, new DOMDataTreeChangeService() {
                @Override
                public <L extends DOMDataTreeChangeListener> ListenerRegistration<L>
                        registerDataTreeChangeListener(final DOMDataTreeIdentifier treeId, final L listener) {
                    final DOMStore publisher = getStore(treeId.getDatastoreType());
                    return ((DOMStoreTreeChangePublisher)publisher).registerTreeChangeListener(
                            treeId.getRootIdentifier(), listener);
                }
            });
        }
        if (datastores.values().stream().allMatch(XPathAwareDOMStore.class::isInstance)) {
            b.put(DOMDataBrokerTransactionXPathSupport.class, (DOMDataBrokerTransactionXPathSupport) (transaction, path, xpath, prefixMapping, callback, callbackExecutor) -> {
               final LogicalDatastoreType storeType = path.getDatastoreType();
               final XPathAwareDOMStore store = (XPathAwareDOMStore) getStore(storeType);
               final DOMStoreReadTransaction txn = extractTransaction(transaction, storeType);
               store.evaluate(txn, path.getRootIdentifier(), xpath, prefixMapping, callback, callbackExecutor);
            });
        }

        extensions = b.build();
    }

    static DOMStoreReadTransaction extractTransaction(final DOMDataTreeReadTransaction transaction,
            final LogicalDatastoreType storeType) {
        checkArgument(transaction instanceof AbstractDOMForwardedCompositeTransaction);
        final DOMStoreTransaction sub = ((AbstractDOMForwardedCompositeTransaction) transaction)
                .getSubtransaction(storeType);
        checkArgument(sub instanceof DOMStoreReadTransaction);
        return (DOMStoreReadTransaction) sub;
    }

    DOMStore getStore(final LogicalDatastoreType storeType) {
        final DOMStore ret = getTxFactories().get(storeType);
        checkState(ret != null, "Requested logical data store %s is not available.", storeType);
        return ret;
    }

    public void setCloseable(final AutoCloseable closeable) {
        this.closeable = closeable;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public void close() {
        super.close();

        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                LOG.debug("Error closing instance", e);
            }
        }
    }

    @Override
    protected Object newTransactionIdentifier() {
        return "DOM-" + txNum.getAndIncrement();
    }

    @Override
    public Map<Class<? extends DOMDataBrokerExtension>, DOMDataBrokerExtension> getSupportedExtensions() {
        return extensions;
    }

    @Override
    public DOMTransactionChain createTransactionChain(final TransactionChainListener listener) {
        checkNotClosed();

        final Map<LogicalDatastoreType, DOMStoreTransactionChain> backingChains =
                new EnumMap<>(LogicalDatastoreType.class);
        for (Entry<LogicalDatastoreType, DOMStore> entry : getTxFactories().entrySet()) {
            backingChains.put(entry.getKey(), entry.getValue().createTransactionChain());
        }

        final long chainId = chainNum.getAndIncrement();
        LOG.debug("Transactoin chain {} created with listener {}, backing store chains {}", chainId, listener,
                backingChains);
        return new DOMDataBrokerTransactionChainImpl(chainId, backingChains, this, listener);
    }
}
