/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataObjectModified;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.ExactDataObjectStep;

final class AdaptedDataObjectModified<T extends DataObject> extends DataObjectModified<T> {
    private final @NonNull AbstractDataObjectModification<T, ?> mod;

    AdaptedDataObjectModified(final @NonNull AbstractDataObjectModification<T, ?> mod) {
        this.mod = requireNonNull(mod);
    }

    @Override
    public ExactDataObjectStep<T> step() {
        return mod.step;
    }

    @Override
    public T dataAfter() {
        return mod.requireDataAfter();
    }

    @Override
    public T dataBefore() {
        return mod.dataBefore();
    }

    @Override
    public <C extends DataObject> DataObjectModification<C> modifiedChild(final ExactDataObjectStep<C> step) {
        return mod.modifiedChild(step);
    }

    @Override
    public Collection<? extends DataObjectModification<?>> modifiedChildren() {
        return mod.modifiedChildren();
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return mod.addToStringAttributes(helper);
    }
}
