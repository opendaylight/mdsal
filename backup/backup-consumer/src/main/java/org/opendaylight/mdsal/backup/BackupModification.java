/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.backup;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackupModification implements org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification {

    private static final Logger LOG = LoggerFactory.getLogger(BackupModification.class);

    private DOMDataTreeWriteTransaction writeTransaction;

    public BackupModification(final DOMDataTreeWriteTransaction transaction) {
        writeTransaction = transaction;
    }

    @Override
    public void delete(YangInstanceIdentifier path) {
        LOG.debug("BackupModification - DELETE - {}", path);
        writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, path);
    }

    @Override
    public void merge(YangInstanceIdentifier path, NormalizedNode<?, ?> data) {

    }

    @Override
    public void write(YangInstanceIdentifier path, NormalizedNode<?, ?> data) {
        LOG.debug("BackupModification - WRITE - {} - DATA: {}", path, data);
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, path, data);
    }

    @Override
    public void ready() {

    }

    @Override
    public void applyToCursor(@NonNull DataTreeModificationCursor cursor) {

    }

    @Override
    public Optional<NormalizedNode<?, ?>> readNode(YangInstanceIdentifier path) {
        return Optional.empty();
    }

    @Override
    public @NonNull DataTreeModification newModification() {
        return null;
    }

    @Override
    public @NonNull SchemaContext getSchemaContext() {
        return null;
    }
}
