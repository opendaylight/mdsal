/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.concepts.Registration;

@Beta
public interface DOMDataTreeShardProducer extends Registration {
    /**
     * Return the collection of tree identifiers to which this producer is bound. This collection
     * is constant during the lifetime of a producer.
     *
     * @return Collection of data tree identifiers.
     */
    @NonNull Collection<DOMDataTreeIdentifier> getPrefixes();

    /**
     * Create a new write transaction for this producer. Any previous transactions need to be closed either via
     * {@link DOMDataTreeShardWriteTransaction#ready()} or cancelled.
     *
     * @return A new write transaction
     * @throws IllegalStateException if a previous transaction has not been closed
     */
    @NonNull DOMDataTreeShardWriteTransaction createTransaction();

    /**
     * Close this producer, releasing all resources. Default implementation does nothing, implementations should provide
     * an implementation.
     */
    // FIXME: 4.0.0: make this method non-default
    @Override
    default void close() {

    }
}
