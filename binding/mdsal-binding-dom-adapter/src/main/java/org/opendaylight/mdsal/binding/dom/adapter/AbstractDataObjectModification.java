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

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.VerifyException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingAugmentationCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.CommonDataObjectCodecTreeNode;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lazily translated {@link DataObjectModification} based on {@link DataTreeCandidateNode}.
 * {@link AbstractDataObjectModification} represents Data tree change event, but whole tree is not translated or
 * resolved eagerly, but only child nodes which are directly accessed by user of data object modification.
 *
 * <p>
 * This class is further specialized as {@link LazyAugmentationModification} and {@link LazyDataObjectModification}, as
 * both use different serialization methods.
 *
 * @param <T> Type of Binding {@link DataObject}
 * @param <N> Type of underlying {@link CommonDataObjectCodecTreeNode}
 */
abstract sealed class AbstractDataObjectModification<T extends DataObject, N extends CommonDataObjectCodecTreeNode<T>>
        implements DataObjectModification<T>
        permits LazyAugmentationModification, LazyDataObjectModification {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataObjectModification.class);

    final @NonNull DataTreeCandidateNode domData;
    final @NonNull PathArgument identifier;
    final @NonNull N codec;

    private volatile ImmutableList<AbstractDataObjectModification<?, ?>> childNodesCache;
    private volatile ModificationType modificationType;

    AbstractDataObjectModification(final DataTreeCandidateNode domData, final N codec, final PathArgument identifier) {
        this.domData = requireNonNull(domData);
        this.identifier = requireNonNull(identifier);
        this.codec = requireNonNull(codec);
    }

    static @Nullable AbstractDataObjectModification<?, ?> from(final CommonDataObjectCodecTreeNode<?> codec,
            final @NonNull DataTreeCandidateNode current) {
        if (codec instanceof BindingDataObjectCodecTreeNode<?> childDataObjectCodec) {
            return new LazyDataObjectModification<>(childDataObjectCodec, current);
        } else if (codec instanceof BindingAugmentationCodecTreeNode<?> childAugmentationCodec) {
            return LazyAugmentationModification.forParent(childAugmentationCodec, current);
        } else {
            throw new VerifyException("Unhandled codec " + codec);
        }
    }

    @Override
    public final Class<T> getDataType() {
        return codec.getBindingClass();
    }

    @Override
    public final PathArgument getIdentifier() {
        return identifier;
    }

    @Override
    public final ModificationType getModificationType() {
        var localType = modificationType;
        if (localType != null) {
            return localType;
        }

        final var domModificationType = domData.getModificationType();
        modificationType = localType = switch (domModificationType) {
            case APPEARED, WRITE -> ModificationType.WRITE;
            case DISAPPEARED, DELETE -> ModificationType.DELETE;
            case SUBTREE_MODIFIED -> resolveSubtreeModificationType();
            default ->
                // TODO: Should we lie about modification type instead of exception?
                throw new IllegalStateException("Unsupported DOM Modification type " + domModificationType);
        };
        return localType;
    }

    @Override
    public final T getDataBefore() {
        return deserialize(domData.getDataBefore());
    }

    @Override
    public final T getDataAfter() {
        return deserialize(domData.getDataAfter());
    }

    private @Nullable T deserialize(final Optional<NormalizedNode> normalized) {
        return normalized.isEmpty() ? null : deserialize(normalized.orElseThrow());
    }

    abstract @Nullable T deserialize(@NonNull NormalizedNode normalized);

    @Override
    public final DataObjectModification<?> getModifiedChild(final PathArgument arg) {
        final var domArgumentList = new ArrayList<YangInstanceIdentifier.PathArgument>();
        final var childCodec = codec.bindingPathArgumentChild(arg, domArgumentList);
        final var toEnter = domArgumentList.iterator();

        // Careful now: we need to validated the first item against subclass
        var current = toEnter.hasNext() ? firstModifiedChild(toEnter.next()) : domData;
        // ... and for everything else we can just go wild
        while (toEnter.hasNext() && current != null) {
            current = current.getModifiedChild(toEnter.next()).orElse(null);
        }

        if (current == null || current.getModificationType() == UNMODIFIED) {
            return null;
        }
        return from(childCodec, current);
    }

    abstract @Nullable DataTreeCandidateNode firstModifiedChild(YangInstanceIdentifier.PathArgument arg);

    @Override
    public final ImmutableList<AbstractDataObjectModification<?, ?>> getModifiedChildren() {
        var local = childNodesCache;
        if (local == null) {
            childNodesCache = local = createModifiedChilden(codec, domData, domChildNodes());
        }
        return local;
    }

    @Override
    public final <C extends ChildOf<? super T>> List<DataObjectModification<C>> getModifiedChildren(
            final Class<C> childType) {
        return streamModifiedChildren(childType).collect(Collectors.toList());
    }

    @Override
    public final <H extends ChoiceIn<? super T> & DataObject, C extends ChildOf<? super H>>
            List<DataObjectModification<C>> getModifiedChildren(final Class<H> caseType, final Class<C> childType) {
        return streamModifiedChildren(childType)
            .filter(child -> caseType.equals(child.identifier.getCaseType().orElse(null)))
            .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private <C extends DataObject> Stream<LazyDataObjectModification<C>> streamModifiedChildren(
            final Class<C> childType) {
        return getModifiedChildren().stream()
            .filter(child -> childType.isAssignableFrom(child.getDataType()))
            .map(child -> (LazyDataObjectModification<C>) child);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <C extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<C>> DataObjectModification<C>
            getModifiedChildListItem(final Class<C> listItem, final K listKey) {
        return (DataObjectModification<C>) getModifiedChild(IdentifiableItem.of(listItem, listKey));
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <H extends ChoiceIn<? super T> & DataObject, C extends Identifiable<K> & ChildOf<? super H>,
            K extends Identifier<C>> DataObjectModification<C> getModifiedChildListItem(final Class<H> caseType,
                    final Class<C> listItem, final K listKey) {
        return (DataObjectModification<C>) getModifiedChild(IdentifiableItem.of(caseType, listItem, listKey));
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <C extends ChildOf<? super T>> DataObjectModification<C> getModifiedChildContainer(
            final Class<C> child) {
        return (DataObjectModification<C>) getModifiedChild(Item.of(child));
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <H extends ChoiceIn<? super T> & DataObject, C extends ChildOf<? super H>> DataObjectModification<C>
            getModifiedChildContainer(final Class<H> caseType, final Class<C> child) {
        return (DataObjectModification<C>) getModifiedChild(Item.of(caseType, child));
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <C extends Augmentation<T> & DataObject> DataObjectModification<C> getModifiedAugmentation(
            final Class<C> augmentation) {
        return (DataObjectModification<C>) getModifiedChild(Item.of(augmentation));
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("identifier", identifier).add("domData", domData);
    }

    abstract @NonNull Collection<DataTreeCandidateNode> domChildNodes();

    private @NonNull ModificationType resolveSubtreeModificationType() {
        return switch (codec.getChildAddressabilitySummary()) {
            case ADDRESSABLE ->
                // All children are addressable, it is safe to report SUBTREE_MODIFIED
                ModificationType.SUBTREE_MODIFIED;
            case UNADDRESSABLE ->
                // All children are non-addressable, report WRITE
                ModificationType.WRITE;
            case MIXED -> {
                // This case is not completely trivial, as we may have NOT_ADDRESSABLE nodes underneath us. If that
                // is the case, we need to turn this modification into a WRITE operation, so that the user is able
                // to observe those nodes being introduced. This is not efficient, but unfortunately unavoidable,
                // as we cannot accurately represent such changes.
                for (DataTreeCandidateNode child : domChildNodes()) {
                    if (BindingStructuralType.recursiveFrom(child) == BindingStructuralType.NOT_ADDRESSABLE) {
                        // We have a non-addressable child, turn this modification into a write
                        yield ModificationType.WRITE;
                    }
                }

                // No unaddressable children found, proceed in addressed mode
                yield ModificationType.SUBTREE_MODIFIED;
            }
        };
    }

    private static @NonNull ImmutableList<AbstractDataObjectModification<?, ?>> createModifiedChilden(
            final CommonDataObjectCodecTreeNode<?> parentCodec, final DataTreeCandidateNode parent,
            final Collection<DataTreeCandidateNode> children) {
        final var result = ImmutableList.<AbstractDataObjectModification<?, ?>>builder();
        populateList(result, parentCodec, parent, children);
        return result.build();
    }

    private static void populateList(final ImmutableList.Builder<AbstractDataObjectModification<?, ?>> result,
            final CommonDataObjectCodecTreeNode<?> parentCodec, final DataTreeCandidateNode parent,
            final Collection<DataTreeCandidateNode> children) {
        final var augmentChildren =
            ArrayListMultimap.<BindingAugmentationCodecTreeNode<?>, DataTreeCandidateNode>create();

        for (var domChildNode : parent.getChildNodes()) {
            if (domChildNode.getModificationType() != UNMODIFIED) {
                final var type = BindingStructuralType.from(domChildNode);
                if (type != BindingStructuralType.NOT_ADDRESSABLE) {
                    /*
                     * Even if type is UNKNOWN, from perspective of BindingStructuralType we try to load codec for it.
                     * We will use that type to further specify debug log.
                     */
                    try {
                        final var childCodec = parentCodec.yangPathArgumentChild(domChildNode.getIdentifier());
                        if (childCodec instanceof BindingDataObjectCodecTreeNode<?> childDataObjectCodec) {
                            populateList(result, type, childDataObjectCodec, domChildNode);
                        } else if (childCodec instanceof BindingAugmentationCodecTreeNode<?> childAugmentationCodec) {
                            // Defer creation once we have collected all modified children
                            augmentChildren.put(childAugmentationCodec, domChildNode);
                        } else {
                            throw new VerifyException("Unhandled codec %s for type %s".formatted(childCodec, type));
                        }
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

        for (var entry : augmentChildren.asMap().entrySet()) {
            final var modification = LazyAugmentationModification.forModifications(entry.getKey(), parent,
                entry.getValue());
            if (modification != null) {
                result.add(modification);
            }
        }
    }

    private static void populateList(final ImmutableList.Builder<AbstractDataObjectModification<?, ?>> result,
            final BindingStructuralType type, final BindingDataObjectCodecTreeNode<?> childCodec,
            final DataTreeCandidateNode domChildNode) {
        switch (type) {
            case INVISIBLE_LIST:
                // We use parent codec intentionally.
                populateListWithSingleCodec(result, childCodec, domChildNode.getChildNodes());
                break;
            case INVISIBLE_CONTAINER:
                populateList(result, childCodec, domChildNode, domChildNode.getChildNodes());
                break;
            case UNKNOWN:
            case VISIBLE_CONTAINER:
                result.add(new LazyDataObjectModification<>(childCodec, domChildNode));
                break;
            default:
        }
    }

    private static void populateListWithSingleCodec(
            final ImmutableList.Builder<AbstractDataObjectModification<?, ?>> result,
            final BindingDataObjectCodecTreeNode<?> codec, final Collection<DataTreeCandidateNode> childNodes) {
        for (var child : childNodes) {
            if (child.getModificationType() != UNMODIFIED) {
                result.add(new LazyDataObjectModification<>(codec, child));
            }
        }
    }
}
