/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.data.tree.api.ModificationType.UNMODIFIED;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingAugmentationCodecTreeNode;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.ExactDataObjectStep;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;

/**
 * Specialization of {@link AbstractDataObjectModification} for {@link Augmentation}s. Is based on a parent
 * {@link DataTreeCandidateNode}, but contains only a subset of its modifications.
 */
final class LazyAugmentationModification<A extends Augmentation<?>>
        extends AbstractDataObjectModification<A, BindingAugmentationCodecTreeNode<A>> {
    private final @NonNull ImmutableList<DataTreeCandidateNode> domChildNodes;

    private LazyAugmentationModification(final BindingAugmentationCodecTreeNode<A> codec,
            final DataTreeCandidateNode parent, final ImmutableList<DataTreeCandidateNode> domChildNodes) {
        super(parent, codec, (ExactDataObjectStep<A>) codec.deserializePathArgument(null));
        this.domChildNodes = requireNonNull(domChildNodes);
    }

    static <A extends Augmentation<?>> @Nullable LazyAugmentationModification<A> forModifications(
            final BindingAugmentationCodecTreeNode<A> codec, final DataTreeCandidateNode parent,
            final Collection<DataTreeCandidateNode> children) {
        // Filter out any unmodified children first
        final var domChildren = children.stream()
            .filter(childMod -> childMod.modificationType() != UNMODIFIED)
            .collect(ImmutableList.toImmutableList());
        // Only return a modification if there is something left
        return domChildren.isEmpty() ? null : new LazyAugmentationModification<>(codec, parent, domChildren);
    }

    static <A extends Augmentation<?>> @Nullable LazyAugmentationModification<A> forParent(
            final BindingAugmentationCodecTreeNode<A> codec, final DataTreeCandidateNode parent) {
        final var builder = ImmutableList.<DataTreeCandidateNode>builder();
        for (var pathArg : codec.childPathArguments()) {
            final var child = parent.modifiedChild(pathArg);
            if (child != null) {
                builder.add(child);
            }
        }
        final var domChildren = builder.build();
        return domChildren.isEmpty() ? null : new LazyAugmentationModification<>(codec, parent, domChildren);
    }

    @Override
    ImmutableList<DataTreeCandidateNode> domChildNodes() {
        return domChildNodes;
    }

    @Override
    org.opendaylight.yangtools.yang.data.tree.api.ModificationType domModificationType() {
        final var before = dataBefore();
        final var after = dataAfter();
        if (before == null) {
            return after == null ? org.opendaylight.yangtools.yang.data.tree.api.ModificationType.UNMODIFIED
                :  org.opendaylight.yangtools.yang.data.tree.api.ModificationType.APPEARED;
        }
        return after == null ? org.opendaylight.yangtools.yang.data.tree.api.ModificationType.DISAPPEARED
            : org.opendaylight.yangtools.yang.data.tree.api.ModificationType.SUBTREE_MODIFIED;
    }


    @Override
    A deserialize(final NormalizedNode normalized) {
        return codec.filterFrom(normalized);
    }

    @Override
    DataTreeCandidateNode firstModifiedChild(final PathArgument arg) {
        // Not entirely efficient linear search, but otherwise we'd have to index, which is even slower
        return domChildNodes.stream()
            .filter(child -> arg.equals(child.name()))
            .findFirst()
            .orElse(null);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper)
            .add("domType", domData.modificationType())
            .add("domChildren", domChildNodes);
    }
}
