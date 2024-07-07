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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreWriteTransaction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.data.tree.api.VersionInfo;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DOMStoreModification implements DataTreeModification {
    private static final Logger LOG = LoggerFactory.getLogger(DOMStoreModification.class);

    private final DOMStoreWriteTransaction transaction;

    DOMStoreModification(final DOMStoreWriteTransaction transaction) {
        this.transaction = requireNonNull(transaction);
    }

    @Override
    public void delete(final YangInstanceIdentifier path) {
        LOG.trace("Delete {}", path);
        transaction.delete(path);
    }

    @Override
    public void write(final YangInstanceIdentifier path, final NormalizedNode data) {
        LOG.trace("Write {} data {}", path, data);
        transaction.write(path, data);
    }

    @Override
    public Optional<NormalizedNode> readNode(final YangInstanceIdentifier path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<VersionInfo> readVersionInfo(final YangInstanceIdentifier path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NonNull DataTreeModification newModification() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EffectiveModelContext modelContext() {
        throw new UnsupportedOperationException();
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
    public void applyToCursor(@NonNull final DataTreeModificationCursor cursor) {
        throw new UnsupportedOperationException();
    }
}
