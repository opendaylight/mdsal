/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShardWriteTransaction;

@Beta
public interface DOMDataTreeShardProducer {
    /**
     * Return the collection of tree identifiers to which this producer is bound. This collection
     * is constant during the lifetime of a producer.
     *
     * @return Collection of data tree identifiers.
     */
    @Nonnull Collection<DOMDataTreeIdentifier> getPrefixes();

    /**
     * Create a new write transaction for this producer. Any previous transactions need to be closed either via
     * {@link DOMDataTreeShardWriteTransaction#ready()} or cancelled.
     *
     * @return A new write transaction
     * @throws IllegalStateException if a previous transaction has not been closed
     */
    DOMDataTreeShardWriteTransaction createTransaction();
}
