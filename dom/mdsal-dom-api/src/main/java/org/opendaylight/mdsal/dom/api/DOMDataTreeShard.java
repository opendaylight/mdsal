/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import java.util.EventListener;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A single shard of the conceptual data tree. This interface defines the basic notifications
 * a shard can receive. Each shard implementation is expected to also implement some of the
 * datastore-level APIs. Which interfaces are required depends on the {@link DOMDataTreeShardingService}
 * implementation.
 */
public interface DOMDataTreeShard extends EventListener {
    /**
     * Invoked whenever a child is getting attached as a more specific prefix under this shard.
     *
     * @param prefix Child's prefix
     * @param child Child shard
     */
    void onChildAttached(@NonNull DOMDataTreeIdentifier prefix, @NonNull DOMDataTreeShard child);

    /**
     * Invoked whenever a child is getting detached as a more specific prefix under this shard.
     *
     * @param prefix Child's prefix
     * @param child Child shard
     */
    void onChildDetached(@NonNull DOMDataTreeIdentifier prefix, @NonNull DOMDataTreeShard child);
}
