/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.xpath;

import com.google.common.annotations.Beta;
import com.google.common.collect.BiMap;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataBrokerExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMServiceExtension;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathResult;

/**
 * {@link DOMDataBrokerExtension} to support XPath-based queries on {@link DOMDataTreeReadTransaction} objects acquired
 * through API contract surface of {@link DOMDataBroker}, including any other {@link DOMServiceExtension}s attached to
 * it.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public interface DOMDataBrokerTransactionXPathSupport extends DOMDataBrokerExtension {
    /**
     * Evaluate an XPath query in the context of a transaction on specified path. This method behaves exactly like
     * {@link DOMDataTreeReadTransaction#read(LogicalDatastoreType, YangInstanceIdentifier)}, except that its output
     * is filtered through provided XPaths conformant to <a href="https://tools.ietf.org/html/rfc7950#section-6.4">
     * YANG 1.1 XPath Evaluation</a>.
     *
     * @param transaction Transaction in which to evaluate the query
     * @param path Path of the tree node in which to evaluate the query
     * @param xpath XPath conforming to RFC7950
     * @param callback Callback to invoke with the evaluation result
     * @param callbackExecutor Executor to use for executing the callback
     * @throws IllegalArgumentException if the transaction is not known to this implementation
     * @throws NullPointerException if any of the arguments is null
     */
    void evaluate(DOMDataTreeReadTransaction transaction, DOMDataTreeIdentifier path, String xpath,
            BiMap<String, QNameModule> prefixMapping, DOMXPathCallback callback, Executor callbackExecutor);

    default FluentFuture<Optional<? extends XPathResult<?>>> evaluate(final DOMDataTreeReadTransaction transaction,
            final DOMDataTreeIdentifier path, final String xpath, final BiMap<String, QNameModule> prefixMapping) {
        final SettableFuture<Optional<? extends XPathResult<?>>> future = SettableFuture.create();
        evaluate(transaction, path, xpath, prefixMapping, DOMXPathCallback.completingFuture(future),
            MoreExecutors.directExecutor());
        return future;
    }
}
