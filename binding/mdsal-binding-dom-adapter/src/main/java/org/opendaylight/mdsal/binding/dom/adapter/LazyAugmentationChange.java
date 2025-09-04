/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;

/**
 * A {@link LazyCandidatenNodeChange} representing a change to an {@link Augmentation} expressed in terms of
 * a {@link DataTreeCandidateNode}.
 */
final class LazyAugmentationChange<T extends Augmentation<?> > extends LazyCandidatenNodeChange<T> {
    private final @NonNull ImmutableList<DataTreeCandidateNode> domChildNodes;

    LazyAugmentationChange(final @NonNull ImmutableList<DataTreeCandidateNode> domChildNodes) {
        this.domChildNodes = requireNonNull(domChildNodes);
    }

    @Override
    Collection<DataTreeCandidateNode> domChildNodes() {
        return domChildNodes;
    }

}
