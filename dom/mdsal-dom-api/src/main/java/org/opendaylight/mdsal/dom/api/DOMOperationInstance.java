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
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * An operation (RPC or action) which is subject to availability. This is a common superclass for {@link Action} and
 * {@link Rpc}.
 *
 * @param <T> type of operation type
 */
public abstract class DOMOperationInstance<T> implements Immutable {
    public static final class Action extends DOMOperationInstance<SchemaPath> {
        private final Set<DOMDataTreeIdentifier> dataTrees;

        public Action(final SchemaPath type, final Set<DOMDataTreeIdentifier> dataTrees) {
            super(type);
            this.dataTrees = ImmutableSet.copyOf(dataTrees);
            checkArgument(!dataTrees.isEmpty());
        }

        /**
         * Return the set of data trees on which this action is available. Note that
         * {@link DOMDataTreeIdentifier#getRootIdentifier()} may be a wildcard.
         *
         * @return Set of trees on which this action is available.
         */
        public Set<DOMDataTreeIdentifier> getDataTrees() {
            return dataTrees;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getType(), dataTrees);
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Action)) {
                return false;
            }
            final Action other = (Action) obj;
            return getType().equals(other.getType()) && dataTrees.equals(other.dataTrees);
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("dataTrees", dataTrees);
        }
    }

    public static final class Rpc extends DOMOperationInstance<QName> {
        public Rpc(final QName type) {
            super(type);
        }

        @Override
        public int hashCode() {
            return getType().hashCode();
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            return this == obj || obj instanceof Rpc && getType().equals(((Rpc) obj).getType());
        }
    }

    private final T type;

    DOMOperationInstance(final T type) {
        this.type = requireNonNull(type);
    }

    /**
     * Return the operation type.
     *
     * @return operation type.
     */
    public final T getType() {
        return type;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(@Nullable Object obj);

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("type", type);
    }
}