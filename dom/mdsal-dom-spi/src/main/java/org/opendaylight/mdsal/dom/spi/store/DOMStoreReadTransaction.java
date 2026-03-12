/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMStore.ReadOperations;
import org.opendaylight.mdsal.dom.api.query.DOMQuery;
import org.opendaylight.mdsal.dom.api.query.DOMQueryResult;
import org.opendaylight.mdsal.dom.spi.query.DOMQueryEvaluator;

public interface DOMStoreReadTransaction extends DOMStoreTransaction, ReadOperations {
    /**
     * {@inheritDoc}
     *
     * <p>Default implementation invokes {@code read(query.getRoot())} and then executes the result with
     * {@link DOMQueryEvaluator}. Implementations are encouraged to provide a more efficient implementation as
     * appropriate.
     */
    @Override
    default @NonNull FluentFuture<DOMQueryResult> execute(final DOMQuery query) {
        return read(query.getRoot()).transform(
            node -> node.map(data -> DOMQueryEvaluator.evaluateOn(query, data)).orElse(DOMQueryResult.of()),
            MoreExecutors.directExecutor());
    }
}
