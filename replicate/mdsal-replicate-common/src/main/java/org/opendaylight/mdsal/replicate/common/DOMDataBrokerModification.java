/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.common;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteOperations;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DOMDataBrokerModification implements DataTreeModification {
    private static final Logger LOG = LoggerFactory.getLogger(DOMDataBrokerModification.class);

    private final DOMDataTreeWriteOperations transaction;
    private final LogicalDatastoreType datastore;

    DOMDataBrokerModification(final DOMDataTreeWriteOperations transaction, final LogicalDatastoreType datastore) {
        this.transaction = requireNonNull(transaction);
        this.datastore = requireNonNull(datastore);
    }

    @Override
    public void delete(final YangInstanceIdentifier path) {
        LOG.trace("BackupModification - DELETE - {}", path);
        transaction.delete(datastore, path);
    }

    @Override
    public void write(final YangInstanceIdentifier path, final NormalizedNode data) {
        LOG.trace("BackupModification - WRITE - {} - DATA: {}", path, data);
        transaction.put(datastore, path, data);
    }

    @Override
    public void merge(final YangInstanceIdentifier path, final NormalizedNode data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void ready() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void applyToCursor(final DataTreeModificationCursor cursor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<NormalizedNode> readNode(final YangInstanceIdentifier path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataTreeModification newModification() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EffectiveModelContext modelContext() {
        throw new UnsupportedOperationException();
    }
}
