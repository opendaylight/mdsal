/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * An action which is subject to availability.
 */
public final class DOMActionInstance implements Immutable {
    private final Set<DOMDataTreeIdentifier> dataTrees;
    private final Absolute type;

    private DOMActionInstance(final Absolute type, final ImmutableSet<DOMDataTreeIdentifier> dataTrees) {
        this.type = requireNonNull(type);
        this.dataTrees = requireNonNull(dataTrees);
        checkArgument(!dataTrees.isEmpty());
    }

    public static DOMActionInstance of(final Absolute type, final Set<DOMDataTreeIdentifier> dataTrees) {
        return new DOMActionInstance(type, ImmutableSet.copyOf(dataTrees));
    }

    public static DOMActionInstance of(final Absolute type, final DOMDataTreeIdentifier... dataTrees) {
        return new DOMActionInstance(type, ImmutableSet.copyOf(dataTrees));
    }

    public static DOMActionInstance of(final Absolute type, final LogicalDatastoreType datastore,
            final YangInstanceIdentifier path) {
        return new DOMActionInstance(type, ImmutableSet.of(DOMDataTreeIdentifier.of(datastore, path)));
    }

    /**
     * Return the set of data trees on which this action is available. These identifiers are required to point
     * to concrete items, i.e. they may not be wildcards. Identifiers which return an empty
     * {@link DOMDataTreeIdentifier#getRootIdentifier()} are considered to match all items in that particular datastore
     * and are expected to be treated as lower-priority alternatives to exact matches.
     *
     * @return Set of trees on which this action is available.
     */
    public Set<DOMDataTreeIdentifier> getDataTrees() {
        return dataTrees;
    }

    /**
     * Return the action type, i.e. the absolute schema node identifier of the action.
     *
     * @return action type.
     */
    public Absolute getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), dataTrees);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof DOMActionInstance other && getType().equals(other.type)
            && dataTrees.equals(other.dataTrees);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", type).add("dataTrees", dataTrees).toString();
    }
}
