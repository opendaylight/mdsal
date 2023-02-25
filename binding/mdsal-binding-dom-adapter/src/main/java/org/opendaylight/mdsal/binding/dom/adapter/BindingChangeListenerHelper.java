/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.dom.codec.impl.AugmentationNodeContext;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Utility artifact containing methods and wrapper/implementation classes serving
 * backward compatibility for augmentation only change listener functionality.
 *
 * <p>
 * Addresses AugmentationNode removal from DOM data structure in ODL YANGTOOLS.
 */
final class BindingChangeListenerHelper {

    private BindingChangeListenerHelper() {
        // utility class
    }

    /**
     * Checks the listener path, wraps listener to dedicated adapter if path paints to Augmentation binding.
     *
     * @param treeId data tree identifier
     * @param delegate the associated data tree change listener
     * @param currentSerializer current adapter serializer
     * @param <T> data object binding type
     * @param <L> data tree change listener type
     * @return wrapper listener if augmentation is addressed, original listener otherwise
     */
    static <T extends DataObject, L extends DataTreeChangeListener<T>> L wrapListener(
        final DataTreeIdentifier<T> treeId, final L delegate, final CurrentAdapterSerializer currentSerializer) {
        final var path = treeId.getRootIdentifier();
        if (Augmentation.class.isAssignableFrom(path.getTargetType())) {
            final var codec = currentSerializer.getSubtreeCodec(path);
            if (codec instanceof AugmentationNodeContext<?> augmContext) {
                return delegate instanceof ClusteredDataTreeChangeListener
                    ? (L) new ClusteredAugmentationListenerWrapper<>(treeId, delegate, augmContext)
                    : (L) new AugmentationListenerWrapper<>(treeId, delegate, augmContext);
            } else {
                throw new IllegalStateException("Unexpected augmentation codec " + codec);
            }
        }
        return delegate;
    }

    private static class AugmentationListenerWrapper<T extends DataObject> implements DataTreeChangeListener<T> {
        private final Class<T> augmentationType;
        private final Set<Class<?>> childTypes;
        private final Set<YangInstanceIdentifier.PathArgument> childPathArgs;
        private final DataTreeChangeListener<T> delegate;

        private AugmentationListenerWrapper(final DataTreeIdentifier<T> treeId,
                final DataTreeChangeListener<T> delegate, final AugmentationNodeContext<?> augmContext) {
            this.augmentationType = treeId.getRootIdentifier().getTargetType();
            this.childPathArgs = augmContext.getChildPaths();
            this.childTypes = augmContext.getChildTypes();
            this.delegate = delegate;
        }

        @Override
        public void onDataTreeChanged(@NonNull Collection<DataTreeModification<T>> changes) {
            final var augmentationOnlyChanges = changes.stream()
                .map(this::extractAugmentationModification).filter(mod -> mod != null)
                .collect(Collectors.toUnmodifiableList());
            if (!augmentationOnlyChanges.isEmpty()) {
                delegate.onDataTreeChanged(augmentationOnlyChanges);
            }
        }

        private @Nullable DataTreeModification<T> extractAugmentationModification(
            final DataTreeModification<T> parentChange) {
            final var parentBefore = parentChange.getRootNode().getDataBefore();
            final var parentAfter = parentChange.getRootNode().getDataAfter();
            final var augmentationBefore = parentBefore instanceof Augmentable<?> au
                ? au.augmentations().get(augmentationType) : null;
            final var augmentationAfter = parentAfter instanceof Augmentable<?> au
                ? au.augmentations().get(augmentationType) : null;
            if (Objects.equals(augmentationBefore, augmentationAfter)) {
                // omit change if augmentation is not changed
                return null;
            }
            // substitute the change of parent node with change of augmentation
            final var treeId = DataTreeIdentifier.create(parentChange.getRootPath().getDatastoreType(),
                parentChange.getRootPath().getRootIdentifier().augmentation((Class) augmentationType));
            return new AugmentationDataTreeModification(treeId, new AugmentationObjectNotification(augmentationType,
                childTypes, childPathArgs, augmentationBefore, augmentationAfter, parentChange.getRootNode()));
        }

    }

    private static final class ClusteredAugmentationListenerWrapper<T extends DataObject>
            extends AugmentationListenerWrapper<T> implements ClusteredDataTreeChangeListener<T> {

        private ClusteredAugmentationListenerWrapper(final DataTreeIdentifier<T> treeId,
            final DataTreeChangeListener<T> delegate, final AugmentationNodeContext<?> augmContext) {
            super(treeId, delegate, augmContext);
        }
    }

    private static final class AugmentationDataTreeModification<T extends DataObject>
            implements DataTreeModification<T> {
        private final @NonNull DataTreeIdentifier<T> path;
        private final @NonNull DataObjectModification<T> rootNode;

        private AugmentationDataTreeModification(final DataTreeIdentifier<T> path,
            final DataObjectModification<T> modification) {
            this.path = requireNonNull(path);
            this.rootNode = requireNonNull(modification);
        }

        @Override
        public @NonNull DataTreeIdentifier<T> getRootPath() {
            return path;
        }

        @Override
        public @NonNull DataObjectModification<T> getRootNode() {
            return rootNode;
        }
    }

    private static final class AugmentationObjectNotification<T extends DataObject>
            implements DataObjectModification<T> {
        final Class<T> dataType;
        final Set<Class<?>> childClasses;
        final Set<InstanceIdentifier.PathArgument> childPathArgs;
        final T dataBefore;
        final T dataAfter;
        final DataObjectModification<T> parent;

        private AugmentationObjectNotification(final Class<T> dataType, final Set<Class<?>> childClasses,
                final Set<InstanceIdentifier.PathArgument> childPathArgs,
                final @Nullable T dataBefore, @Nullable final T dataAfter, final DataObjectModification<T> parent) {
            this.dataType = dataType;
            this.childClasses = childClasses;
            this.childPathArgs = childPathArgs;
            this.dataBefore = dataBefore;
            this.dataAfter = dataAfter;
            this.parent = parent;
        }

        @Override
        public InstanceIdentifier.PathArgument getIdentifier() {
            return InstanceIdentifier.Item.of(dataType);
        }

        @Override
        public @NonNull Class<T> getDataType() {
            return dataType;
        }

        @Override
        public @NonNull ModificationType getModificationType() {
            if (dataBefore == null) {
                return ModificationType.WRITE;
            }
            if (dataAfter == null) {
                return ModificationType.DELETE;
            }
            return ModificationType.SUBTREE_MODIFIED;
        }

        @Override
        public @Nullable T getDataBefore() {
            return dataBefore;
        }

        @Override
        public @Nullable T getDataAfter() {
            return dataAfter;
        }

        @Override
        public @NonNull Collection<? extends DataObjectModification<? extends DataObject>> getModifiedChildren() {
            return parent.getModifiedChildren().stream()
                .filter(mod -> childClasses.contains(mod.getIdentifier().getType()))
                .collect(Collectors.toUnmodifiableSet());
        }

        @Override
        public <C extends ChildOf<? super T>> Collection<DataObjectModification<C>>
                getModifiedChildren(@NonNull Class<C> childType) {
            return childClasses.contains(childType) ? parent.getModifiedChildren(childType) : Set.of();
        }

        @Override
        public <H extends ChoiceIn<? super T> & DataObject, C extends ChildOf<? super H>>
                Collection<DataObjectModification<C>> getModifiedChildren(
                    @NonNull Class<H> caseType, @NonNull Class<C> childType) {
            return childClasses.contains(childType) ? parent.getModifiedChildren(caseType, childType) : Set.of();
        }

        @Override
        public @Nullable <H extends ChoiceIn<? super T> & DataObject,
                C extends ChildOf<? super H>> DataObjectModification<C> getModifiedChildContainer(
                    @NonNull Class<H> caseType, @NonNull Class<C> child) {
            return childClasses.contains(child) ? parent.getModifiedChildContainer(caseType, child) : null;
        }

        @Override
        public @Nullable <C extends ChildOf<? super T>> DataObjectModification<C> getModifiedChildContainer(
                @NonNull Class<C> child) {
            return childClasses.contains(child) ? parent.getModifiedChildContainer(child) : null;
        }

        @Override
        public @Nullable <C extends Augmentation<T> & DataObject> DataObjectModification<C> getModifiedAugmentation(
                @NonNull Class<C> augmentation) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @Nullable <N extends Identifiable<K> & ChildOf<? super T>,
                K extends Identifier<N>> DataObjectModification<N> getModifiedChildListItem(
                    @NonNull Class<N> listItem, @NonNull K listKey) {
            return childClasses.contains(listItem) ? parent.getModifiedChildListItem(listItem, listKey) : null;
        }

        @Override
        public @Nullable <H extends ChoiceIn<? super T> & DataObject, C extends Identifiable<K> & ChildOf<? super H>,
                K extends Identifier<C>> DataObjectModification<C> getModifiedChildListItem(
                    final @NonNull Class<H> caseType, final @NonNull Class<C> listItem, final @NonNull K listKey) {
            return childClasses.contains(listItem)
                ? parent.getModifiedChildListItem(caseType, listItem, listKey) : null;
        }

        @Override
        public @Nullable DataObjectModification<? extends DataObject> getModifiedChild(
                InstanceIdentifier.PathArgument childArgument) {
            return childPathArgs.contains(childArgument) ? parent.getModifiedChild(childArgument) : null;
        }
    }
}
