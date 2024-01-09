/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A reference to a {@link DataContainer} type forming a single step in a path similar to {@code instance-identifier}.
 *
 * @param <T> DataContainer type
 */
/*
 * FIXME: this interface forms a partial model of the following RFC7950 construct:
 *
 *          ;; Instance Identifiers
 *
 *          instance-identifier = 1*("/" (node-identifier [1*key-predicate / leaf-list-predicate / pos]))
 *
 * We handle the 'node-identifier for DataObjects' and 'key-predicate' cases. What is missing are interfaces for:
 * - 'leaf-list-predicate' (exact value match)
 * - 'pos' interfaces (index into a list or a leaf-list}
 * - 'node-identifier for non-DataObjects' (i.e. leaf, anydata, anyxml)
 */
public sealed interface ContainerStep<T extends DataContainer> extends Comparable<ContainerStep<?>>, Serializable
        permits ExactContainerStep, KeylessStep {
    /**
     * Return the data object type backing this PathArgument.
     *
     * @return Data object type
     */
    @NonNull Class<T> type();

    /**
     * Return an optional enclosing case type. This is used only when {@link #type()} references a node defined
     * in a {@code grouping} which is reference inside a {@code case} statement in order to safely reference the node.
     *
     * @return case class, or {@code null}
     */
    @Nullable Class<? extends DataObject> caseType();

    @Override
    default int compareTo(final ContainerStep<?> other) {
        final var typeCmp = compareClasses(type(), other.type());
        if (typeCmp != 0) {
            return typeCmp;
        }
        final var caseType = caseType();
        final var otherCaseType = other.caseType();
        if (caseType == null) {
            return otherCaseType == null ? 0 : -1;
        }
        final var caseCmp = otherCaseType == null ? 1 : compareClasses(caseType, otherCaseType);
        return caseCmp != 0 ? caseCmp : compareHierarchy(this, other);
    }

    private static int compareHierarchy(final ContainerStep<?> recv, final ContainerStep<?> other) {
        if (recv instanceof NodeStep) {
            // lowest
            if (other instanceof NodeStep) {
                return 0;
            } else if (other instanceof ChoiceStep || other instanceof KeyStep || other instanceof KeylessStep) {
                return -1;
            }
        } else if (recv instanceof ChoiceStep) {
            // second lowest
            if (other instanceof ChoiceStep) {
                return 0;
            } else if (other instanceof NodeStep) {
                return 1;
            } else if (other instanceof KeyStep || other instanceof KeylessStep) {
                return -1;
            }
        } else if (recv instanceof KeyStep thisAware) {
            // second highest
            if (other instanceof KeyStep otherAware) {
                @SuppressWarnings("unchecked")
                final var thisKey = (Comparable<Object>) thisAware.key();
                return thisKey.compareTo(otherAware.key());
            } else if (other instanceof NodeStep || other instanceof ChoiceStep) {
                return 1;
            } else if (other instanceof KeylessStep) {
                return -1;
            }
        } else if (recv instanceof KeylessStep) {
            // highest
            if (other instanceof KeylessStep) {
                return 0;
            } else if (other instanceof NodeStep || other instanceof ChoiceStep || other instanceof KeyStep) {
                return 1;
            }
        }
        throw new IllegalStateException("Unhandled " + recv + ".compareTo(" + other + ")");
    }

    private static int compareClasses(final Class<?> first, final Class<?> second) {
        return first.getCanonicalName().compareTo(second.getCanonicalName());
    }
}
