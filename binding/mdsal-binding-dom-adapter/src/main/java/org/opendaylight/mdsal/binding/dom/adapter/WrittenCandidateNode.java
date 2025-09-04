/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataObjectWritten;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.ExactDataObjectStep;

final class WrittenCandidateNode<T extends DataObject> implements DataObjectWritten<T> {
    private final CandidateNodeAdapter<T, ?> adapter;

    WrittenCandidateNode(final @NonNull CandidateNodeAdapter<T, ?> adapter) {
        this.adapter = requireNonNull(adapter);
    }

    @Override
    public ExactDataObjectStep<T> step() {
       return adapter.step;
    }

    @Override
    public <C extends DataObject> DataObjectModification<C> modifiedChild(final ExactDataObjectStep<C> step) {
        return adapter.modifiedChild(step);
    }

    @Override
    public Collection<? extends DataObjectModification<?>> modifiedChildren() {
        return adapter.modifiedChildren();
    }

    @Override
    public T dataBefore() {
        return adapter.dataBefore();
    }

    @Override
    public T dataAfter() {
        return adapter.dataAfter();
    }
}
