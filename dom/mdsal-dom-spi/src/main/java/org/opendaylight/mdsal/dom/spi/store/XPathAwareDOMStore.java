/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import com.google.common.annotations.Beta;
import com.google.common.collect.BiMap;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.xpath.DOMXPathCallback;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * A DOMStore which can evaluate XPath expressions.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public interface XPathAwareDOMStore {
    /**
     * Evaluate an XPath query in the context of a transaction on specified path.
     *
     * @param transaction Transaction in which to evaluate the query
     * @param path Path of the tree node in which to evaluate the query
     * @param xpath XPath conforming to RFC7950
     * @param prefixMapping Prefix mapping to be used for lookups
     * @param callback Callback to invoke with the evaluation result
     * @param callbackExecutor Executor to use for executing the callback
     * @throws IllegalArgumentException if the transaction is not known to this implementation
     * @throws NullPointerException if any of the arguments is null
     */
    void evaluate(DOMStoreReadTransaction transaction, YangInstanceIdentifier path, String xpath,
            BiMap<String, QNameModule> prefixMapping, DOMXPathCallback callback, Executor callbackExecutor);
}
