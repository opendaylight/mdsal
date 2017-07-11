/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import com.google.common.annotations.Beta;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prefix table indexed by {@link DOMDataTreeIdentifier}.
 * Stores values in tree and provides lookup of closest ancestor
 *
 * @param <V> Value type
 */
@Beta
@NotThreadSafe
public final class DOMDataTreePrefixTable<V> {

    private static final Logger LOG = LoggerFactory.getLogger(DOMDataTreePrefixTable.class);
    private final Map<LogicalDatastoreType, DOMDataTreePrefixTableEntry<V>> roots =
        new EnumMap<>(LogicalDatastoreType.class);

    private DOMDataTreePrefixTable() {

    }

    public static <V> DOMDataTreePrefixTable<V> create() {
        return new DOMDataTreePrefixTable<>();
    }

    /**
     * Lookups entry by provided {@link DOMDataTreeIdentifier}, if entry is not present returns
     * closest non-null entry towards root or null if no entry towards root exists.
     *
     * @param prefix Prefix for lookup
     * @return closest non-null entry towards root or null if no entry towards root exists.
     */
    @Nullable
    public DOMDataTreePrefixTableEntry<V> lookup(@Nonnull final DOMDataTreeIdentifier prefix) {
        final DOMDataTreePrefixTableEntry<V> t = roots.get(prefix.getDatastoreType());
        if (t == null) {
            return null;
        }

        return t.lookup(prefix.getRootIdentifier());
    }

    /**
     * Stores value associated to the prefix.
     *
     * @param prefix DOM prefix of value
     * @param value Value to be stored
     * @throws IllegalStateException If value is already stored for provided prefix
     */
    public void store(@Nonnull final DOMDataTreeIdentifier prefix, @Nonnull final V value) {
        DOMDataTreePrefixTableEntry<V> domDataTreePrefixTableEntry = roots.get(prefix.getDatastoreType());
        if (domDataTreePrefixTableEntry == null) {
            domDataTreePrefixTableEntry = new DOMDataTreePrefixTableEntry<>();
            roots.put(prefix.getDatastoreType(), domDataTreePrefixTableEntry);
        }

        domDataTreePrefixTableEntry.store(prefix.getRootIdentifier(), value);
    }

    /**
     * Removes value associated to the prefix.
     * Value is removed only and only if full prefix match for stored value. Removal of prefix does
     * not remove child prefixes.
     *
     * @param prefix to be removed
     */
    public void remove(@Nonnull final DOMDataTreeIdentifier prefix) {
        LOG.trace("Entering DOMDataTreePrefixTable#remove");
        final DOMDataTreePrefixTableEntry<V> t = roots.get(prefix.getDatastoreType());
        if (t == null) {
            LOG.warn("Shard registration {} points to non-existent table", t);
            return;
        }

        LOG.trace("Calling remove on DOMDataTreePrefixTableEntry {}", t);
        t.remove(prefix.getRootIdentifier());
    }

}
