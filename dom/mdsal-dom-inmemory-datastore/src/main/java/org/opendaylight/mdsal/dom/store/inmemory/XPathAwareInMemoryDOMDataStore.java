/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.collect.BiMap;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import javax.xml.xpath.XPathException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.xpath.DOMXPathCallback;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreReadTransaction;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedTransaction;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedTransactionXPathSupport;
import org.opendaylight.mdsal.dom.spi.store.XPathAwareDOMStore;
import org.opendaylight.yangtools.concepts.CheckedValue;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathResult;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathSchemaContextFactory;

@Beta
@NonNullByDefault
public class XPathAwareInMemoryDOMDataStore extends InMemoryDOMDataStore implements XPathAwareDOMStore {
    private final SnapshotBackedTransactionXPathSupport xpathSupport;

    public XPathAwareInMemoryDOMDataStore(final String name, final ExecutorService dataChangeListenerExecutor,
            final int maxDataChangeListenerQueueSize, final boolean debugTransactions,
            final XPathSchemaContextFactory xpathContextFactory) {
        super(name, dataChangeListenerExecutor, maxDataChangeListenerQueueSize, debugTransactions);
        xpathSupport = SnapshotBackedTransactionXPathSupport.forXPathContextFactory(xpathContextFactory);
    }

    @Override
    public final void evaluate(final DOMStoreReadTransaction transaction, final YangInstanceIdentifier path,
            final String xpath, final BiMap<String, QNameModule> prefixMapping, final DOMXPathCallback callback,
            final Executor callbackExecutor) {
        checkArgument(transaction instanceof SnapshotBackedTransaction, "Unsupported transaction %s", transaction);

        final CheckedValue<Optional<? extends XPathResult<?>>, XPathException> result = xpathSupport.evaluate(
            (SnapshotBackedTransaction) transaction, path, xpath, prefixMapping);
        callbackExecutor.execute(() -> callback.accept(result));
    }
}
