/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import com.google.common.base.Optional;
import com.google.common.collect.ForwardingObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshotCursor;

/**
 * Delegating implementation of a {@link ReadableCursorOperation}.
 */
abstract class DelegatingReadableCursorOperation extends ForwardingObject implements ReadableCursorOperation {

    @Override
    protected abstract DataTreeSnapshotCursor delegate();

    @Override
    public Optional<NormalizedNode<?, ?>> readNode(final PathArgument arg) {
        return Optional.fromJavaUtil(delegate().readNode(arg));
    }

    @Override
    public void exit() {
        delegate().exit();
    }

    @Override
    public DelegatingReadableCursorOperation enter(final PathArgument arg) {
        delegate().enter(arg);
        return this;
    }
}