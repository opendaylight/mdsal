/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;

@Beta
public final class DOMDataTreeShardRegistration<T extends DOMDataTreeShard> extends AbstractListenerRegistration<T> {
    private final DOMDataTreeIdentifier prefix;
    private final ShardedDOMDataTree tree;

    public DOMDataTreeShardRegistration(final ShardedDOMDataTree tree, final DOMDataTreeIdentifier prefix,
            final T shard) {
        super(shard);
        this.tree = requireNonNull(tree);
        this.prefix = requireNonNull(prefix);
    }

    public DOMDataTreeIdentifier getPrefix() {
        return prefix;
    }

    @Override
    protected void removeRegistration() {
        tree.removeShard(this);
    }
}
