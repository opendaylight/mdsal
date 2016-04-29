/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@NotThreadSafe
public final class DOMDataTreePrefixTableEntry<V> implements Identifiable<PathArgument> {
    private static final Logger LOG = LoggerFactory.getLogger(DOMDataTreePrefixTableEntry.class);
    // FIXME: We do probably want to adapt map
    private final Map<PathArgument, DOMDataTreePrefixTableEntry<V>> children = new HashMap<>();
    private final PathArgument identifier;
    private V value;

    DOMDataTreePrefixTableEntry() {
        identifier = null;
    }

    DOMDataTreePrefixTableEntry(final PathArgument identifier) {
        this.identifier = Preconditions.checkNotNull(identifier);
    }

    @Override
    public PathArgument getIdentifier() {
        return identifier;
    }

    public V getValue() {
        return value;
    }

    DOMDataTreePrefixTableEntry<V> lookup(final YangInstanceIdentifier id) {
        final Iterator<PathArgument> it = id.getPathArguments().iterator();
        DOMDataTreePrefixTableEntry<V> entry = this;
        DOMDataTreePrefixTableEntry<V> lastPresentEntry = entry;

        while (it.hasNext()) {
            final PathArgument a = it.next();
            final DOMDataTreePrefixTableEntry<V> child = entry.children.get(a);
            if (child == null) {
                LOG.debug("Lookup of {} stopped at {}", id, a);
                break;
            }

            entry = child;

            if (child.getValue() != null) {
                lastPresentEntry = child;
            }
        }

        return lastPresentEntry;
    }

    void store(final YangInstanceIdentifier id, final V reg) {
        final Iterator<PathArgument> it = id.getPathArguments().iterator();
        DOMDataTreePrefixTableEntry<V> entry = this;

        while (it.hasNext()) {
            final PathArgument a = it.next();
            DOMDataTreePrefixTableEntry<V> child = entry.children.get(a);
            if (child == null) {
                child = new DOMDataTreePrefixTableEntry<>(a);
                entry.children.put(a, child);
            }
            // TODO: Is this correct? We want to enter child
            entry = child;
        }

        Preconditions.checkState(entry.value == null);
        entry.value = reg;
    }

    private boolean remove(final Iterator<PathArgument> it) {
        if (it.hasNext()) {
            final PathArgument arg = it.next();
            final DOMDataTreePrefixTableEntry<V> child = children.get(arg);
            if (child != null) {
                if (child.remove(it)) {
                    children.remove(arg);
                }
            } else {
                LOG.warn("Cannot remove non-existent child {}", arg);
            }
        } else {
            /*
             * Iterator is empty, this effectively means is table entry to remove registration.
             * FIXME: We probably want to compare value to make sure we are removing correct value.
             */
            value = null;
        }
        return value == null && children.isEmpty();
    }

    void remove(final YangInstanceIdentifier id) {
        this.remove(id.getPathArguments().iterator());
    }
}
