/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.ExactDataObjectStep;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A change to a {@link DataObject} with the addressing information required to form a {@link DataObjectIdentifier}.
 *
 * @apiNote
 *     This interface has a {@code C} type parameter and a {@link #asContract()} method. The parameter ensures that
 *     something like {@code interface Foo extends DataObjectDeleted, DataObjectWritten} cannot happen.
 *
 * @param <T> the {@link DataObject} type
 * @param <S> the {@code non-sealed} specialization of {@link DataObjectChange} this object represents
 */
public sealed interface DataObjectChange<T extends DataObject, S extends DataObjectChange<T, S>> extends Immutable
        permits DataObjectDeleted, DataObjectChange.WithDataAfter {
    /**
     * A {@link DataObjectChange} after which there is the instance value available.
     *
     * @param <T> the {@link DataObject} type
     * @param <S> the {@code non-sealed} specialization of {@link DataObjectChange} this object represents
     */
    sealed interface WithDataAfter<T extends DataObject, S extends WithDataAfter<T, S>> extends DataObjectChange<T, S>
            permits DataObjectModified, DataObjectWritten {
        @Override
        S asContract();

        /**
         * {@return the after-image}
         */
        @NonNull T dataAfter();
    }

    /**
     * {@return this object as the contract it represents}
     * @implSpec
     *     This method must be implemented by each {@code non-sealed} specialization of this contract as
     *     {@snippet :
     *     @Override
     *     default INTERFACE_NAME<T> asContract() {
     *         return this;
     *     }
     *     }
     */
    S asContract();

    /**
     * {@return the {@link ExactDataObjectStep} step along {@link DataObjectIdentifier#steps()} axis this change
     * corresponds to}
     */
    @NonNull ExactDataObjectStep<T> steo();
}