/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.modification;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.api.TreeNodeModification;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingTreeNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.base.IdentifiableItem;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.mdsal.binding.javav2.spec.structural.TreeChildNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lazily translated {@link TreeNodeModification} based on {@link DataTreeCandidateNode}.
 *
 * <p>
 * {@link LazyTreeNodeModification} represents Data tree change event, but whole tree is not translated or
 * resolved eagerly, but only child nodes which are directly accessed by user of tree node modification.
 *
 * @param <T>
 *            Type of Binding Tree Node
 */
@Beta
final class LazyTreeNodeModification<T extends TreeNode> implements TreeNodeModification<T> {

    private static final Logger LOG = LoggerFactory.getLogger(LazyTreeNodeModification.class);

    private final BindingTreeNodeCodec<T> codec;
    private final DataTreeCandidateNode domData;
    private final TreeArgument<?> identifier;
    private Collection<TreeNodeModification<? extends TreeNode>> childNodesCache;

    private LazyTreeNodeModification(final BindingTreeNodeCodec<T> codec, final DataTreeCandidateNode domData) {
        this.codec = requireNonNull(codec);
        this.domData = requireNonNull(domData);
        this.identifier = codec.deserializePathArgument(domData.getIdentifier());
    }

    static <T extends TreeNode> TreeNodeModification<T> create(final BindingTreeNodeCodec<T> codec,
            final DataTreeCandidateNode domData) {
        return new LazyTreeNodeModification<>(codec, domData);
    }

    private static Collection<TreeNodeModification<? extends TreeNode>> from(final BindingTreeNodeCodec<?> parentCodec,
            final Collection<DataTreeCandidateNode> domChildNodes) {
        final List<TreeNodeModification<? extends TreeNode>> result = new ArrayList<>(domChildNodes.size());
        populateList(result, parentCodec, domChildNodes);
        return result;
    }

    private static void populateList(final List<TreeNodeModification<? extends TreeNode>> result,
            final BindingTreeNodeCodec<?> parentCodec, final Collection<DataTreeCandidateNode> domChildNodes) {
        for (final DataTreeCandidateNode domChildNode : domChildNodes) {
            final BindingStructuralType type = BindingStructuralType.from(domChildNode);
            if (type != BindingStructuralType.NOT_ADDRESSABLE) {
                /*
                 * Even if type is UNKNOWN, from perspective of BindingStructuralType we try to load codec for
                 * it. We will use that type to further specify debug log.
                 */
                try {
                    final BindingTreeNodeCodec<?> childCodec =
                            parentCodec.yangPathArgumentChild(domChildNode.getIdentifier());
                    populateList(result, type, childCodec, domChildNode);
                } catch (final IllegalArgumentException e) {
                    if (type == BindingStructuralType.UNKNOWN) {
                        LOG.debug("Unable to deserialize unknown DOM node {}", domChildNode, e);
                    } else {
                        LOG.debug("Binding representation for DOM node {} was not found", domChildNode, e);
                    }
                }
            }
        }
    }

    private static void populateList(final List<TreeNodeModification<? extends TreeNode>> result,
            final BindingStructuralType type, final BindingTreeNodeCodec<?> childCodec,
            final DataTreeCandidateNode domChildNode) {
        switch (type) {
            case INVISIBLE_LIST:
                // We use parent codec intentionally.
                populateListWithSingleCodec(result, childCodec, domChildNode.getChildNodes());
                break;
            case INVISIBLE_CONTAINER:
                populateList(result, childCodec, domChildNode.getChildNodes());
                break;
            case UNKNOWN:
            case VISIBLE_CONTAINER:
                result.add(create(childCodec, domChildNode));
                break;
            default:
        }
    }

    private static void populateListWithSingleCodec(final List<TreeNodeModification<? extends TreeNode>> result,
            final BindingTreeNodeCodec<?> codec, final Collection<DataTreeCandidateNode> childNodes) {
        for (final DataTreeCandidateNode child : childNodes) {
            result.add(create(codec, child));
        }
    }

    @Nullable
    @Override
    public T getDataBefore() {
        return deserialize(domData.getDataBefore());
    }

    @Nullable
    @Override
    public T getDataAfter() {
        return deserialize(domData.getDataAfter());
    }

    @Nonnull
    @Override
    public Class<T> getDataType() {
        return codec.getBindingClass();
    }

    @Nonnull
    @Override
    public TreeArgument<?> getIdentifier() {
        return identifier;
    }

    @Nonnull
    @Override
    public TreeNodeModification.ModificationType getModificationType() {
        switch (domData.getModificationType()) {
            case APPEARED:
            case WRITE:
                return TreeNodeModification.ModificationType.WRITE;
            case SUBTREE_MODIFIED:
                return TreeNodeModification.ModificationType.SUBTREE_MODIFIED;
            case DISAPPEARED:
            case DELETE:
                return TreeNodeModification.ModificationType.DELETE;

            default:
                // TODO: Should we lie about modification type instead of exception?
                throw new IllegalStateException("Unsupported DOM Modification type " + domData.getModificationType());
        }
    }

    @Nonnull
    @Override
    public Collection<TreeNodeModification<? extends TreeNode>> getModifiedChildren() {
        if (childNodesCache == null) {
            childNodesCache = from(codec, domData.getChildNodes());
        }
        return childNodesCache;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends TreeChildNode<? super T, ?>> Collection<TreeNodeModification<C>>
            getModifiedChildren(@Nonnull final Class<C> childType) {
        final List<TreeNodeModification<C>> children = new ArrayList<>();
        for (final TreeNodeModification<? extends TreeNode> potential : getModifiedChildren()) {
            if (childType.isAssignableFrom(potential.getDataType())) {
                children.add((TreeNodeModification<C>) potential);
            }
        }
        return children;
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    @Override
    public TreeNodeModification<? extends TreeNode> getModifiedChild(final TreeArgument childArgument) {
        final List<YangInstanceIdentifier.PathArgument> domArgumentList = new ArrayList<>();
        final BindingTreeNodeCodec<?> childCodec = codec.bindingPathArgumentChild(childArgument, domArgumentList);
        final Iterator<YangInstanceIdentifier.PathArgument> toEnter = domArgumentList.iterator();
        DataTreeCandidateNode current = domData;
        while (toEnter.hasNext() && current != null) {
            current = current.getModifiedChild(toEnter.next());
        }
        if (current != null) {
            return create(childCodec, current);
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <C extends IdentifiableItem<T, K> & TreeChildNode<? super T, ?>, K extends IdentifiableItem<T, K>>
            TreeNodeModification<C>
            getModifiedChildListItem(@Nonnull final Class<C> listItem, @Nonnull final K listKey) {
        return (TreeNodeModification) getModifiedChild(new IdentifiableItem(listItem, listKey));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Nullable
    @Override
    public <C extends TreeChildNode<? super T, ?>> TreeNodeModification<C>
            getModifiedChildContainer(@Nonnull final Class<C> child) {
        return (TreeNodeModification<C>) getModifiedChild(new Item(child));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Nullable
    @Override
    public <C extends Augmentation<T> & TreeNode> TreeNodeModification<C>
            getModifiedAugmentation(@Nonnull final Class<C> augmentation) {
        return (TreeNodeModification<C>) getModifiedChild(new Item(augmentation));
    }

    private T deserialize(final Optional<NormalizedNode<?, ?>> dataAfter) {
        if (dataAfter.isPresent()) {
            return codec.deserialize(dataAfter.get());
        }
        return null;
    }
}

