/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static java.util.Objects.requireNonNull;

import java.io.ObjectStreamException;
import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyAware;

/**
 * A {@link KeyAware}-based step with a {@link #key()}. It equates to a {@code node-identifier} with a
 * {@code key-predicate}.
 *
 * @param <K> Key type
 * @param <T> KeyAware type
 */
@Deprecated(since = "14.0.0", forRemoval = true)
record KeyStep<K extends Key<T>, T extends KeyAware<K> & DataObject>(
        @NonNull Class<T> type,
        @Nullable Class<? extends DataObject> caseType,
        @NonNull K key) implements Serializable {
    KeyStep {
        NodeStep.checkType(type, true);
        NodeStep.checkCaseType(caseType);
        requireNonNull(key);
    }

    @java.io.Serial
    Object readResolve() throws ObjectStreamException {
        return new org.opendaylight.yangtools.binding.KeyStep<>(type, caseType, key);
    }
}
