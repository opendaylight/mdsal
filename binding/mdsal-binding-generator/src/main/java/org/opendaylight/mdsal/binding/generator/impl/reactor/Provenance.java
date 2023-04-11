/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.AddedByUsesAware;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Statement provenance. This captures the four basic types of how an effective YANG statement relates to the declared
 * model.
 */
enum Provenance {
    /**
     * An explicitly-declared statement. Originating module is completely cognizant of its existence and hence its
     * presence is reflected in the originating module's namespace. These statements are manifested as normal Java
     * interfaces and methods.
     */
    ORIGINAL(false, false),
    /**
     * A statement added by a top-level {@code augment} statement. The originating module may be the same as the module
     * containing the {@code augment}, but that really is an anti-pattern. In any case, these statements are manifested
     * as being encapsulated by an {@code Augmentation} interface.
     */
    AUGMENTATION(false, true),
    /**
     * A statement that appears as the effect of an {@code uses} statement. In Binding terms that means that
     * the corresponding methods are inherited as part of implementing the interface generated for the correspoding
     * {@code grouping} statement.
     */
    USES(true, false),
    /**
     * A statement that appears as the effect of an {@code augment} statement used within a {@code uses} statement. As
     * such it ends up generating an Augmentation of the target node.
     */
    USES_AUGMENTATION(true, true);

    private final boolean addedByUses;
    private final boolean augmenting;

    Provenance(final boolean addedByUses, final boolean augmenting) {
        this.addedByUses = addedByUses;
        this.augmenting = augmenting;
    }

    // FIXME: this should be coming from AbstractCompositeGenerator instead
    @Deprecated(forRemoval = true)
    static @NonNull Provenance of(final EffectiveStatement<?, ?> statement) {
        if (statement instanceof AddedByUsesAware aware) {
            if (statement instanceof CopyableNode copyable && copyable.isAugmenting()) {
                return aware.isAddedByUses() ? USES_AUGMENTATION : AUGMENTATION;
            } else if (aware.isAddedByUses()) {
                return USES;
            }
        }
        return ORIGINAL;
    }

    final boolean addedByUses() {
        return addedByUses;
    }

    final boolean augmenting() {
        return augmenting;
    }
}
