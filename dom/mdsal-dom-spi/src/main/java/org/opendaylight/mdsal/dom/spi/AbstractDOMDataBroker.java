/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTransactionChain;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTreeChangePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDOMDataBroker extends AbstractDOMForwardedTransactionFactory<DOMStore>
        implements PingPongMergingDOMDataBroker {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDOMDataBroker.class);

    private final AtomicLong txNum = new AtomicLong();
    private final AtomicLong chainNum = new AtomicLong();
    private final @NonNull List<Extension> supportedExtensions;

    private volatile AutoCloseable closeable;

    protected AbstractDOMDataBroker(final Map<LogicalDatastoreType, DOMStore> datastores) {
        super(datastores);

        boolean treeChange = true;
        for (var ds : datastores.values()) {
            if (!(ds instanceof DOMStoreTreeChangePublisher)) {
                treeChange = false;
                break;
            }
        }

        if (treeChange) {
            supportedExtensions = List.of((DOMDataTreeChangeService) (treeId, listener) -> {
                final var dsType = treeId.datastore();
                if (getTxFactories().get(dsType) instanceof DOMStoreTreeChangePublisher publisher) {
                    return publisher.registerTreeChangeListener(treeId.path(), listener);
                }
                throw new IllegalStateException("Publisher for " + dsType + " data store is not available");
            });
        } else {
            supportedExtensions = List.of();
        }
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
}
