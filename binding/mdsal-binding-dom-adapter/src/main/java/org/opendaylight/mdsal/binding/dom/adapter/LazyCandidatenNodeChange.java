/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.DataObjectChange;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;

/**
 * A base class for projecting {@link DataObjectChange} view of a {@link DataTreeCandidateNode}.
 */
sealed class LazyCandidatenNodeChange<T extends DataObject> permits LazyAugmentationChange {

    @NonNullByDefault
    Collection<DataTreeCandidateNode> domChildNodes() {
        return domData.childNodes();
    }

    @Override
    @NonNull ModificationType domModificationType() {
        return domData.modificationType();
    }

    @Override
    T deserialize(final NormalizedNode normalized) {
        return codec.deserialize(normalized);
    }

    @Override
    DataTreeCandidateNode firstModifiedChild(final PathArgument arg) {
        return domData.modifiedChild(arg);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper).add("domData", domData);
    }


    abstract @Nullable T deserialize(@NonNull NormalizedNode normalized);



}
