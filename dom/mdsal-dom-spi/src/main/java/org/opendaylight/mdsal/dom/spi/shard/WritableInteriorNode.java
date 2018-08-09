/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

class WritableInteriorNode extends WriteableNodeWithSubshard {

    private final PathArgument identifier;

    WritableInteriorNode(final PathArgument identifier, final Map<PathArgument, WriteableModificationNode> children) {
        super(children);
        this.identifier = requireNonNull(identifier);
    }

    @Override
    public PathArgument getIdentifier() {
        return identifier;
    }

    @Override
    public WriteCursorStrategy createOperation(final DOMDataTreeWriteCursor parentCursor) {
        return new WritableNodeOperation(this, parentCursor) {
            @Override
            public void exit() {
                // We are not root, so we can safely exit our level.
                getCursor().exit();
            }
        };
    }

}