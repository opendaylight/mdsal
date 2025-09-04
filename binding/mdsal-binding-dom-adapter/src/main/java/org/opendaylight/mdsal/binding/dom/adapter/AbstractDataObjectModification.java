/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.data.tree.api.ModificationType.UNMODIFIED;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
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
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.ExactDataObjectStep;
import org.opendaylight.yangtools.binding.data.codec.api.BindingAugmentationCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingChoiceCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataContainerCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.CommonDataObjectCodecTreeNode;
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
 * <p>This class is further specialized as {@link LazyAugmentationModification} and {@link LazyDataObjectModification},
 * as both use different serialization methods.
 *
 * @param <T> Type of Binding {@link DataObject}
 * @param <N> Type of underlying {@link CommonDataObjectCodecTreeNode}
 */
abstract sealed class AbstractDataObjectModification<T extends DataObject, N extends CommonDataObjectCodecTreeNode<T>>
        permits LazyAugmentationModification, LazyDataObjectModification {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataObjectModification.class);
    private static final @NonNull Object NULL_DATA_OBJECT = new Object();
    private static final VarHandle MODIFICATION_TYPE;
    private static final VarHandle MODIFIED_CHILDREN;
    private static final VarHandle DATA_BEFORE;
    private static final VarHandle DATA_AFTER;

    static {
        final var lookup = MethodHandles.lookup();

        try {
            MODIFICATION_TYPE = lookup.findVarHandle(AbstractDataObjectModification.class, "modificationType",
                ModificationType.class);
            MODIFIED_CHILDREN = lookup.findVarHandle(AbstractDataObjectModification.class, "modifiedChildren",
                ImmutableList.class);
            DATA_BEFORE = lookup.findVarHandle(AbstractDataObjectModification.class, "dataBefore", Object.class);
            DATA_AFTER = lookup.findVarHandle(AbstractDataObjectModification.class, "dataAfter", Object.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    final @NonNull DataTreeCandidateNode domData;
    final @NonNull ExactDataObjectStep<T> step;
    final @NonNull N codec;

    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile ImmutableList<AbstractDataObjectModification<?, ?>> modifiedChildren;
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile ModificationType modificationType;
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile Object dataBefore;
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile Object dataAfter;

    AbstractDataObjectModification(final DataTreeCandidateNode domData, final N codec,
            final ExactDataObjectStep<T> step) {
        this.domData = requireNonNull(domData);
        this.step = requireNonNull(step);
        this.codec = requireNonNull(codec);
    }

    static <T extends DataObject> @Nullable DataObjectModification<T> from(final CommonDataObjectCodecTreeNode<T> codec,
            final @NonNull DataTreeCandidateNode current) {
        @SuppressWarnings("unchecked")
        final var mod = (AbstractDataObjectModification<T, ?>) switch (codec) {
            case BindingDataObjectCodecTreeNode<?> childDataObjectCodec ->
                new LazyDataObjectModification<>(childDataObjectCodec, current);
            case BindingAugmentationCodecTreeNode<?> childAugmentationCodec ->
                LazyAugmentationModification.forParent(childAugmentationCodec, current);
            default -> throw new VerifyException("Unhandled codec " + codec);
        };
        return mod == null ? null : mod.toModification();
    }

    final @NonNull DataObjectModification<T> toModification() {
        return switch (modificationType()) {
            case DELETE -> new AdaptedDataObjectDeleted<>(this);
            case SUBTREE_MODIFIED -> new AdaptedDataObjectModified<>(this);
            case WRITE -> new AdaptedDataObjectWritten<>(this);
        };
    }

    final ModificationType modificationType() {
        final var local = (ModificationType) MODIFICATION_TYPE.getAcquire(this);
        return local != null ? local : loadModificationType();
    }

    private @NonNull ModificationType loadModificationType() {
        final var domModificationType = domModificationType();
        final var computed = switch (domModificationType) {
            case APPEARED, WRITE -> ModificationType.WRITE;
            case DISAPPEARED, DELETE -> ModificationType.DELETE;
            case SUBTREE_MODIFIED -> resolveSubtreeModificationType();
            default ->
                // TODO: Should we lie about modification type instead of exception?
                throw new IllegalStateException("Unsupported DOM Modification type " + domModificationType);
        };

        MODIFICATION_TYPE.setRelease(this, computed);
        return computed;
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

    final @NonNull T requireDataAfter() {
        return verifyNotNull(dataAfter());
    }

    private @Nullable T loadDataAfter() {
        final var computed = deserializeNullable(domData.dataAfter());
        final var witness = DATA_AFTER.compareAndExchangeRelease(this, null, mask(computed));
        return witness == null ? computed : unmask(witness);
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

    final <C extends DataObject> DataObjectModification<C> modifiedChild(final ExactDataObjectStep<C> arg) {
        final var domArgumentList = new ArrayList<YangInstanceIdentifier.PathArgument>();
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
        final var ret = (DataObjectModification<C>) from(childCodec, current);
        return ret;
    }

    abstract @Nullable DataTreeCandidateNode firstModifiedChild(YangInstanceIdentifier.PathArgument arg);

    final ImmutableList<DataObjectModification<?>> modifiedChildren() {
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

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("step", step).add("domData", domData);
    }

    abstract @NonNull Collection<DataTreeCandidateNode> domChildNodes();

    abstract org.opendaylight.yangtools.yang.data.tree.api.@NonNull ModificationType domModificationType();

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
                for (var child : domChildNodes()) {
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
                result.add(modification.toModification());
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
                result.add(new LazyDataObjectModification<>(childCodec, domChildNode).toModification());
                break;
            default:
        }
    }

    private static void populateListWithSingleCodec(final ImmutableList.Builder<DataObjectModification<?>> result,
            final BindingDataObjectCodecTreeNode<?> codec, final Collection<DataTreeCandidateNode> childNodes) {
        for (var child : childNodes) {
            if (child.modificationType() != UNMODIFIED) {
                result.add(new LazyDataObjectModification<>(codec, child).toModification());
            }
        }
    }
}
