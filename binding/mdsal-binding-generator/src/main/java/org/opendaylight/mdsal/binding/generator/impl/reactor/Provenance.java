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
 * Statement provenance.
 *
 */
enum Provenance {
    ORIGINAL(false, false),
    AUGMENTATION(false, true),
    USES(true, false),
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
