/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collection;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.ExactDataObjectStep;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;

/**
 * Lazily translated {@link DataObjectModification} based on {@link DataTreeCandidateNode}.
 *
 * <p>
 * {@link LazyDataObjectModification} represents Data tree change event, but whole tree is not translated or resolved
 * eagerly, but only child nodes which are directly accessed by user of data object modification.
 *
 * @param <T> Type of Binding Data Object
 */
final class LazyDataObjectModification<T extends DataObject>
        extends AbstractDataObjectModification<T, BindingDataObjectCodecTreeNode<T>> {
    LazyDataObjectModification(final BindingDataObjectCodecTreeNode<T> codec, final DataTreeCandidateNode domData) {
        super(domData, codec, (ExactDataObjectStep<T>) codec.deserializePathArgument(domData.name()));
    }

    @Override
    Collection<DataTreeCandidateNode> domChildNodes() {
        return domData.childNodes();
    }

    @Override
    org.opendaylight.yangtools.yang.data.tree.api.ModificationType domModificationType() {
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
}
