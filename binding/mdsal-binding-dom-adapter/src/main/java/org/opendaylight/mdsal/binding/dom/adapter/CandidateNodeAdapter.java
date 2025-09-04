/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.data.tree.api.ModificationType.UNMODIFIED;

import com.google.common.base.VerifyException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.ExactDataObjectStep;
import org.opendaylight.yangtools.binding.data.codec.api.BindingAugmentationCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingChoiceCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataContainerCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.CommonDataObjectCodecTreeNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link DataTreeCandidateNode} as projected into binding {@link DataObjectModification}.
 */
abstract sealed class CandidateNodeAdapter<T extends DataObject, N extends CommonDataObjectCodecTreeNode<T>> {
   private static final class OfAugmentation<T extends Augmentation<?>>
            extends CandidateNodeAdapter<T,  BindingAugmentationCodecTreeNode<T>> {
        private final @NonNull ImmutableList<DataTreeCandidateNode> domChildNodes;

        private OfAugmentation(final BindingAugmentationCodecTreeNode<T> codec, final DataTreeCandidateNode parent,
                final ImmutableList<DataTreeCandidateNode> domChildNodes) {
            super(codec, parent, (ExactDataObjectStep<T>) codec.deserializePathArgument(null));
            this.domChildNodes = requireNonNull(domChildNodes);
        }

        @Override
        T deserialize(final NormalizedNode normalized) {
            return codec.filterFrom(normalized);
        }

        @Override
        ModificationType domModificationType() {
            final var before = dataBefore();
            final var after = dataAfter();
            if (before == null) {
                return after == null ? ModificationType.UNMODIFIED : ModificationType.APPEARED;
            }
            return after == null ? ModificationType.DISAPPEARED : ModificationType.SUBTREE_MODIFIED;
        }

        @Override
        Collection<DataTreeCandidateNode> domChildNodes() {
            return domChildNodes;
        }

        @Override
        DataTreeCandidateNode firstModifiedChild(final PathArgument arg) {
            // Not entirely efficient linear search, but otherwise we'd have to index, which is even slower
            return domChildNodes.stream()
                .filter(child -> arg.equals(child.name()))
                .findFirst()
                .orElse(null);
        }
    }

    private static final class Regular<T extends DataObject>
            extends CandidateNodeAdapter<T, BindingDataObjectCodecTreeNode<T>> {
        Regular(final BindingDataObjectCodecTreeNode<T> codec, final DataTreeCandidateNode domData) {
            super(codec, domData, (ExactDataObjectStep<T>) codec.deserializePathArgument(domData.name()));
        }

        @Override
        T deserialize(final NormalizedNode normalized) {
            return codec.deserialize(normalized);
        }

        @Override
        ModificationType domModificationType() {
            return domData.modificationType();
        }

        @Override
        Collection<DataTreeCandidateNode> domChildNodes() {
            return domData.childNodes();
        }

        @Override
        DataTreeCandidateNode firstModifiedChild(final PathArgument arg) {
            return domData.modifiedChild(arg);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(CandidateNodeAdapter.class);
    private static final @NonNull Object NULL_DATA_OBJECT = new Object();
    private static final VarHandle MODIFIED_CHILDREN;
    private static final VarHandle DATA_BEFORE;
    private static final VarHandle DATA_AFTER;

    static {
        final var lookup = MethodHandles.lookup();

        try {
            MODIFIED_CHILDREN = lookup
                .findVarHandle(CandidateNodeAdapter.class, "modifiedChildren", ImmutableList.class);
            DATA_BEFORE = lookup.findVarHandle(CandidateNodeAdapter.class, "dataBefore", Object.class);
            DATA_AFTER = lookup.findVarHandle(CandidateNodeAdapter.class, "dataAfter", Object.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    final @NonNull DataTreeCandidateNode domData;
    final @NonNull ExactDataObjectStep<T> step;
    final @NonNull N codec;

    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile Object dataBefore;
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile Object dataAfter;

    CandidateNodeAdapter(final N codec, final DataTreeCandidateNode domData, final ExactDataObjectStep<T> step) {
        this.codec = requireNonNull(codec);
        this.domData = requireNonNull(domData);
        this.step = requireNonNull(step);
    }

    static @Nullable CandidateNodeAdapter<?, ?> of(final @NonNull CommonDataObjectCodecTreeNode<?> codec,
            final @NonNull DataTreeCandidateNode candidate) {
        return switch (codec) {
            case BindingDataObjectCodecTreeNode<?> dataObjectCodec -> new Regular<>(dataObjectCodec, candidate);
            case BindingAugmentationCodecTreeNode<?> augmentationCodec -> {
                final var builder = ImmutableList.<DataTreeCandidateNode>builder();
                for (var pathArg : augmentationCodec.childPathArguments()) {
                    final var child = candidate.modifiedChild(pathArg);
                    if (child != null) {
                        builder.add(child);
                    }
                }
                final var domChildren = builder.build();
                yield domChildren.isEmpty() ? null : new OfAugmentation<>(augmentationCodec, candidate, domChildren);
            }
            default -> throw new VerifyException("Unhandled codec " + codec);
        };
    }

    final @Nullable DataObjectModification<T> toModification() {
        return switch (domModificationType()) {
            case UNMODIFIED -> null;
            case APPEARED, WRITE -> new WrittenCandidateNode<>(this);
            case DISAPPEARED, DELETE -> new DeletedCandidateNode<>(this);
            case SUBTREE_MODIFIED ->
                switch (codec.getChildAddressabilitySummary()) {
                    // All children are addressable, it is safe to report SUBTREE_MODIFIED
                    case ADDRESSABLE -> new ModifiedCandidateNode<>(this);
                    // All children are non-addressable, report WRITE
                    case UNADDRESSABLE -> new WrittenCandidateNode<>(this);
                    case MIXED -> {
                        // This case is not completely trivial, as we may have NOT_ADDRESSABLE nodes underneath us. If
                        // that is the case, we need to turn this modification into a WRITE operation, so that the user
                        // is able to observe those nodes being introduced. This is not efficient, but unfortunately
                        // unavoidable, as we cannot accurately represent such changes.
                        for (var child : domChildNodes()) {
                            if (BindingStructuralType.recursiveFrom(child) == BindingStructuralType.NOT_ADDRESSABLE) {
                                // We have a non-addressable child, turn this modification into a write
                                yield new WrittenCandidateNode<>(this);
                            }
                        }

                        // No unaddressable children found, proceed in addressed mode
                        yield new ModifiedCandidateNode<>(this);
                    }
                };
        };
    }

    final @Nullable T dataBefore() {
        final var local = DATA_BEFORE.getAcquire(this);
        return local != null ? unmask(local) : loadDataBefore();
    }

    private @Nullable T loadDataBefore() {
        final var computed = deserializeNullable(domData.dataBefore());
        final var witness = DATA_BEFORE.compareAndExchangeRelease(this, null, mask(computed));
        return witness == null ? computed : unmask(witness);
    }

    final @Nullable T dataAfter() {
        final var local = DATA_AFTER.getAcquire(this);
        return local != null ? unmask(local) : loadDataAfter();
    }

    private @Nullable T loadDataAfter() {
        final var computed = deserializeNullable(domData.dataAfter());
        final var witness = DATA_AFTER.compareAndExchangeRelease(this, null, mask(computed));
        return witness == null ? computed : unmask(witness);
    }

    final <C extends DataObject> @Nullable DataObjectModification<C> modifiedChild(final ExactDataObjectStep<C> arg) {
        final var domArgumentList = new ArrayList<PathArgument>();
        final var childCodec = codec.bindingPathArgumentChild(arg, domArgumentList);
        final var toEnter = domArgumentList.iterator();

        // Careful now: we need to validated the first item against subclass
        var current = toEnter.hasNext() ? firstModifiedChild(toEnter.next()) : domData;
        // ... and for everything else we can just go wild
        while (toEnter.hasNext() && current != null) {
            current = current.modifiedChild(toEnter.next());
        }

        if (current == null || current.modificationType() == UNMODIFIED) {
            return null;
        }

        @SuppressWarnings("unchecked")
        final var adapter = (CandidateNodeAdapter<C, ?>) of(childCodec, current);
        return adapter == null ? null : adapter.toModification();
    }

    final @NonNull ImmutableList<DataObjectModification<?>> modifiedChildren() {
        final var local = (ImmutableList<DataObjectModification<?>>) MODIFIED_CHILDREN.getAcquire(this);
        return local != null ? local : loadModifiedChilden();
    }

    @SuppressWarnings("unchecked")
    private @NonNull ImmutableList<DataObjectModification<?>> loadModifiedChilden() {
        final var builder = ImmutableList.<DataObjectModification<?>>builder();
        populateList(builder, codec, domData, domChildNodes());
        final var computed = builder.build();
        // Non-trivial return: use CAS to ensure we reuse concurrent loads
        final var witness = MODIFIED_CHILDREN.compareAndExchangeRelease(this, null, computed);
        return witness == null ? computed : (ImmutableList<DataObjectModification<?>>) witness;
    }

    private static void populateList(final ImmutableList.Builder<DataObjectModification<?>> result,
            final BindingDataContainerCodecTreeNode<?> parentCodec, final DataTreeCandidateNode parent,
            final Collection<DataTreeCandidateNode> children) {
        final var augmentChildren =
            ArrayListMultimap.<BindingAugmentationCodecTreeNode<?>, DataTreeCandidateNode>create();

        for (var domChildNode : parent.childNodes()) {
            if (domChildNode.modificationType() != UNMODIFIED) {
                final var type = BindingStructuralType.from(domChildNode);
                if (type != BindingStructuralType.NOT_ADDRESSABLE) {
                    /*
                     * Even if type is UNKNOWN, from perspective of BindingStructuralType we try to load codec for it.
                     * We will use that type to further specify debug log.
                     */
                    try {
                        final var childCodec = parentCodec.yangPathArgumentChild(domChildNode.name());
                        switch (childCodec) {
                            case BindingDataObjectCodecTreeNode<?> childDataObjectCodec ->
                            populateList(result, type, childDataObjectCodec, domChildNode);
                            case BindingAugmentationCodecTreeNode<?> childAugmentationCodec ->
                            // Defer creation once we have collected all modified children
                            augmentChildren.put(childAugmentationCodec, domChildNode);
                            case BindingChoiceCodecTreeNode<?> childChoiceCodec ->
                            populateList(result, childChoiceCodec, domChildNode, domChildNode.childNodes());
                            default ->
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

    private static void populateList(final ImmutableList.Builder<DataObjectModification<?>> result,
            final BindingStructuralType type, final BindingDataObjectCodecTreeNode<?> childCodec,
            final DataTreeCandidateNode domChildNode) {
        switch (type) {
            case INVISIBLE_LIST:
                // We use parent codec intentionally.
                populateListWithSingleCodec(result, childCodec, domChildNode.childNodes());
                break;
            case INVISIBLE_CONTAINER:
                populateList(result, childCodec, domChildNode, domChildNode.childNodes());
                break;
            case UNKNOWN, VISIBLE_CONTAINER:
                final var mod = new Regular<>(childCodec, domChildNode).toModification();
                if (mod != null) {
                    result.add();
                }
                break;
            default:
        }
    }

    private static void populateListWithSingleCodec(final ImmutableList.Builder<DataObjectModification<?>> result,
            final BindingDataObjectCodecTreeNode<?> codec, final Collection<DataTreeCandidateNode> childNodes) {
        for (var child : childNodes) {
            final var childMod = new Regular<>(codec, child).toModification();
            if (childMod != null) {
                result.add(childMod);
            }
        }
    }

    private static <T extends DataObject> @NonNull Object mask(final @Nullable T obj) {
        return obj != null ? obj : NULL_DATA_OBJECT;
    }

    @SuppressWarnings("unchecked")
    private @Nullable T unmask(final @NonNull Object obj) {
        return obj == NULL_DATA_OBJECT ? null : (T) verifyNotNull(obj);
    }

    private @Nullable T deserializeNullable(final @Nullable NormalizedNode normalized) {
        return normalized == null ? null : deserialize(normalized);
    }

    abstract @Nullable T deserialize(@NonNull NormalizedNode normalized);

    abstract @NonNull ModificationType domModificationType();

    abstract @NonNull Collection<DataTreeCandidateNode> domChildNodes();

    abstract @Nullable DataTreeCandidateNode firstModifiedChild(@NonNull PathArgument arg);
}
