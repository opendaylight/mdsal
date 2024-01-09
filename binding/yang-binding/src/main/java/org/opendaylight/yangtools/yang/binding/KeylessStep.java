/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link KeyAware}-based step without the corresponding key. This corresponds to a {@code node-identifier} step,
 * where we know there is a {@code key-predicate} possible, but we do not have it.
 *
 * @param <T> KeyAware type
 */
public record KeylessStep<T extends KeyAware<?> & DataObject>(
        @NonNull Class<T> type,
        @Nullable Class<? extends DataObject> caseType) implements DataObjectStep<T> {
    @java.io.Serial
    private static final long serialVersionUID = 0;

    public KeylessStep {
        NodeIdentifierStep.checkType(type, true);
        NodeIdentifierStep.checkCaseType(caseType);
    }

    public KeylessStep(final @NonNull Class<T> type) {
        this(type, null);
    }

    // FIXME: this should be a
    boolean matches(final @NonNull DataObjectStep<?> other) {
        // FIXME: instanceof check for KeyPredicateStep, then a match -- i.e. reject match on plain NodeIdentifierStep,
        //        because that is an addressing mismaatch
        return type.equals(other.type()) && Objects.equals(caseType, other.caseType());
    }
}
