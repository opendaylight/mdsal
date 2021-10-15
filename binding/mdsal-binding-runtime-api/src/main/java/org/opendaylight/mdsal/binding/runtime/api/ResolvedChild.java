/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * A child in Binding hierarchy. It has differing behaviours, but its core purpose is to tie together parent's
 * instantiation and child type. Normally there would be a 1:1 mapping between EffectiveModelContext constructs and
 * their child nodes, but with groupings and cases in play, this becomes more of a meeting place.
 */
@Beta
public abstract class ResolvedChild implements Immutable {
    static final class Regular extends ResolvedChild {
        Regular(final DataSchemaNode schema) {
            super(schema);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Item<?> createItem(final Class<?> childClass) {
            return Item.of((Class<? extends DataObject>) childClass);
        }
    }

    static final class UsedCase extends ResolvedChild {
        private final Class<? extends DataObject> parent;

        UsedCase(final Class<? extends DataObject> parent, final DataSchemaNode schema) {
            super(schema);
            this.parent = requireNonNull(parent);
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Item<?> createItem(final Class<?> childClass) {
            return Item.of((Class) parent, (Class) childClass);
        }
    }

    public final @NonNull DataSchemaNode schema;

    private ResolvedChild(final DataSchemaNode schema) {
        this.schema = requireNonNull(schema);
    }

    public abstract @NonNull Item<?> createItem(Class<?> childClass);
}
