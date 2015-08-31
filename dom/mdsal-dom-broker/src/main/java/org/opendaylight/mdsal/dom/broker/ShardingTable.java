/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import java.util.EnumMap;
import java.util.Map;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ShardingTable<V> {

    private static final Logger LOG = LoggerFactory.getLogger(ShardingTable.class);
    private final Map<LogicalDatastoreType, ShardingTableEntry<V>> roots = new EnumMap<>(LogicalDatastoreType.class);

    private ShardingTable() {

    }

    static <V> ShardingTable<V> create() {
        return new ShardingTable<>();
    }

    ShardingTableEntry<V> lookup(final DOMDataTreeIdentifier prefix) {
        final ShardingTableEntry<V> t = roots.get(prefix.getDatastoreType());
        if (t == null) {
            return null;
        }

        return t.lookup(prefix.getRootIdentifier());
    }

    void store(final DOMDataTreeIdentifier prefix, final V reg) {
        ShardingTableEntry<V> t = roots.get(prefix.getDatastoreType());
        if (t == null) {
            t = new ShardingTableEntry<V>();
            roots.put(prefix.getDatastoreType(), t);
        }

        t.store(prefix.getRootIdentifier(), reg);
    }

    void remove(final DOMDataTreeIdentifier prefix) {
        final ShardingTableEntry<V> t = roots.get(prefix.getDatastoreType());
        if (t == null) {
            LOG.warn("Shard registration {} points to non-existent table", t);
            return;
        }

        t.remove(prefix.getRootIdentifier());
    }

}
