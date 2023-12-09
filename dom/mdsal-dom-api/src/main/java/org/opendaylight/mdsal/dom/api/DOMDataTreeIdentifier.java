/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.HierarchicalIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * A unique identifier for a particular subtree. It is composed of the logical data store type and the instance
 * identifier of the root node.
 */
@NonNullByDefault
public final class DOMDataTreeIdentifier implements HierarchicalIdentifier<DOMDataTreeIdentifier>,
        Comparable<DOMDataTreeIdentifier> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final YangInstanceIdentifier rootIdentifier;
    private final LogicalDatastoreType datastoreType;

    public DOMDataTreeIdentifier(final LogicalDatastoreType datastoreType,
            final YangInstanceIdentifier rootIdentifier) {
        this.datastoreType = requireNonNull(datastoreType);
        this.rootIdentifier = requireNonNull(rootIdentifier);
    }

    /**
     * Return the logical data store type.
     *
     * @return Logical data store type. Guaranteed to be non-null.
     */
    public LogicalDatastoreType getDatastoreType() {
        return datastoreType;
    }

    /**
     * Return the {@link YangInstanceIdentifier} of the root node.
     *
     * @return Instance identifier corresponding to the root node.
     */
    public YangInstanceIdentifier getRootIdentifier() {
        return rootIdentifier;
    }

    @Override
    public boolean contains(final DOMDataTreeIdentifier other) {
        return datastoreType == other.datastoreType && rootIdentifier.contains(other.rootIdentifier);
    }

    public DOMDataTreeIdentifier toOptimized() {
        final var opt = rootIdentifier.toOptimized();
        return opt == rootIdentifier ? this : new DOMDataTreeIdentifier(datastoreType, opt);
    }

    @Override
    public int hashCode() {
        return datastoreType.hashCode() * 31 + rootIdentifier.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof DOMDataTreeIdentifier other && datastoreType == other.datastoreType
            && rootIdentifier.equals(other.rootIdentifier);
    }

    @Override
    public int compareTo(final DOMDataTreeIdentifier domDataTreeIdentifier) {
        int cmp = datastoreType.compareTo(domDataTreeIdentifier.datastoreType);
        if (cmp != 0) {
            return cmp;
        }

        final var myIter = rootIdentifier.getPathArguments().iterator();
        final var otherIter = domDataTreeIdentifier.rootIdentifier.getPathArguments().iterator();

        while (myIter.hasNext()) {
            if (!otherIter.hasNext()) {
                return 1;
            }

            final var myPathArg = myIter.next();
            final var otherPathArg = otherIter.next();
            cmp = myPathArg.compareTo(otherPathArg);
            if (cmp != 0) {
                return cmp;
            }
        }

        return otherIter.hasNext() ? -1 : 0;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("datastore", datastoreType).add("root", rootIdentifier).toString();
    }

    @java.io.Serial
    Object writeReplace() {
        return new DTIv1(this);
    }
}
