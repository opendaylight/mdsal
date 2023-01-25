/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;

public class WildcardedInstanceIdentifierBuilder<T extends DataObject> implements
        InstanceIdentifierBuilder<T> {

    @Override
    public @NonNull <N extends ChildOf<? super T>> InstanceIdentifierBuilder<N> child(
            Class<N> container) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public @NonNull <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>>
    InstanceIdentifierBuilder<N> child(Class<C> caze, Class<N> container) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public @NonNull <N extends Identifiable<K> & ChildOf<? super T>,
            K extends Identifier<N>> InstanceIdentifierBuilder<N> child(Class<@NonNull N> listItem, K listKey) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public @NonNull <C extends ChoiceIn<? super T> & DataObject, K extends Identifier<N>,
            N extends Identifiable<K> & ChildOf<? super C>> InstanceIdentifierBuilder<N> child(
            Class<C> caze, Class<N> listItem, K listKey) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <N extends ChildOf<? super T> & Identifiable<?>> WildcardedInstanceIdentifierBuilder<N> wildcardChild(
            Class<N> container) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public @NonNull <N extends DataObject & Augmentation<? super T>> InstanceIdentifierBuilder<N> augmentation(
            Class<N> container) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public @NonNull InstanceIdentifier<T> build() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
