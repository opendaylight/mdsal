/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collection;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.ChoiceIn;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.ExactDataObjectStep;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyStep;
import org.opendaylight.yangtools.binding.NodeStep;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Modified Data Object. Represents a modification of DataObject, which has a few kinds as indicated by
 * {@link #modificationType()}.
 *
 * @param <T> Type of modified object
 */
public abstract sealed class DataObjectModification<T extends DataObject> implements Immutable
        permits DataObjectDeleted, DataObjectModification.WithDataAfter {
    /**
     * Represents type of modification which has occurred.
     *
     * @deprecated Use a enhanced switch over {@link DataObjectModification} type hierarchy instead.
     */
    @Deprecated(since = "15.0.0")
    public enum ModificationType {
        /**
         * Child node (direct or indirect) was modified.
         */
        SUBTREE_MODIFIED,
        /**
         * Node was explicitly created / overwritten.
         */
        WRITE,
        /**
         * Node was deleted.
         */
        DELETE
    }

    /**
     * A {@link DataObjectModification} after which there is the instance value available.
     *
     * @param <T> the {@link DataObject} type
     * @since 15.0.0
     */
    public abstract static sealed class WithDataAfter<T extends DataObject> extends DataObjectModification<T>
            permits DataObjectModified, DataObjectWritten {
        WithDataAfter(final @NonNull ModificationType type) {
            super(type);
        }

        @Override
        @SuppressWarnings("deprecation")
        public abstract @NonNull T dataAfter();
    }

    private final @NonNull ModificationType modificationType;

    DataObjectModification(final @NonNull ModificationType modificationType) {
        this.modificationType = requireNonNull(modificationType);
    }

    /**
     * Returns type of modified object.
     *
     * @return type of modified object.
     */
    public final @NonNull Class<T> dataType() {
        return step().type();
    }

    /**
     * {@return the {@link ExactDataObjectStep} step along {@link DataObjectIdentifier#steps()} axis this modification
     * corresponds to}
     */
    public abstract @NonNull ExactDataObjectStep<T> step();

    /**
     * Returns {@link #step()} as a {@link KeyStep} of specified type.
     *
     * @param <E> expected {@link EntryObject} type
     * @param <K> expected {@link Key} type
     * @param type the {@link EntryObject} class
     * @return a {@link KeyStep}
     * @throws ClassCastException if {@code type} does not represent an {@link EntryObject}
     * @throws IllegalArgumentException if {@code type} does not match {@code step().type()} or {@code step()} is not
     *         a {@link KeyStep}.
     * @since 14.0.19
     */
    @Beta
    // TODO: this method is quite ugly, but necessary for user convenience. We should be providing a better alternative
    //       in the form of
    //
    //       interface EntryObjectModification<E, K> extends DataObjectModification<E> {
    //           @Override
    //           ListStep<K, E> step();
    //       }
    //
    //       That requires an similar specialization of DataTreeChangeListener, so that users registering for listening
    //       on EntryObjects can get this out of the box.
    public final <E extends EntryObject<E, K>, K extends Key<E>> @NonNull KeyStep<K, E> coerceKeyStep(
            final @NonNull Class<E> type) {
        final var cast = type.asSubclass(EntryObject.class);
        final var step = step();
        if (!(step instanceof KeyStep<?, ?> keyStep)) {
            throw new IllegalArgumentException("Cannot coerce " + step);
        }
        if (!cast.equals(keyStep.type())) {
            throw new IllegalArgumentException(keyStep + " does not match type " + type.getName());
        }

        @SuppressWarnings("unchecked")
        final var ret = (KeyStep<K, E>) keyStep;
        return ret;
    }

    /**
     * Returns type of modification.
     *
     * @return type of performed modification.
     * @deprecated Use a enhanced switch over {@link DataObjectModification} type hierarchy instead.
     */
    @Deprecated(since = "15.0.0")
    public final @NonNull ModificationType modificationType() {
        return modificationType;
    }

    /**
     * Returns before-state of top level container. Implementations are encouraged, but not required to provide this
     * state.
     *
     * @return State of object before modification. Null if subtree was not present, or the implementation cannot
     *         provide the state.
     */
    public abstract @Nullable T dataBefore();

    /**
     * Returns after-state of top level container.
     *
     * @return State of object after modification. Null if subtree is not present.
     * @deprecated Use a enhanced switch over {@link DataObjectModification} type hierarchy instead.
     */
    @Deprecated(since = "15.0.0")
    public abstract @Nullable T dataAfter();

    /**
     * Returns a child modification if a node identified by {@code childArgument} was modified by this modification.
     *
     * @param step {@link ExactDataObjectStep} of child node
     * @return Modification of child identified by {@code step} if it was modified, {@code null} otherwise
     * @throws NullPointerException if {@code step} is {@code null}
     * @throws IllegalArgumentException if the step does not represent a valid child according to generated model
     */
    public abstract <C extends DataObject> @Nullable DataObjectModification<C> modifiedChild(
        @NonNull ExactDataObjectStep<C> step);

    /**
     * {@return unmodifiable collection of modified direct children}
     */
    public abstract @NonNull Collection<? extends @NonNull DataObjectModification<?>> modifiedChildren();

    /**
     * Returns a collection of child modifications matching {@code childType}.
     *
     * @param childType the child type
     * @return All child modifications matching {@code childType}
     * @throws NullPointerException if {@code childType} is null
     * @throws ClassCastException if {@code childType} is not a subclass of {@link ChildOf}
     * @throws IllegalArgumentException if @code childType} class is not valid child according to generated model
     */
    public final <C extends ChildOf<? super T>> @NonNull Collection<DataObjectModification<C>> getModifiedChildren(
            final @NonNull Class<C> childType) {
        final var checkedChild = childType.asSubclass(ChildOf.class);
        @SuppressWarnings("unchecked")
        final var ret = (@NonNull Collection<DataObjectModification<C>>) modifiedChildren().stream()
            .filter(child -> checkedChild.isAssignableFrom(child.dataType()))
            .collect(Collectors.toUnmodifiableList());
        return ret;
    }

    /**
     * Returns a collection of child modifications matching {@code childType}. This method should be used if the child
     * is defined in a grouping brought into a case inside this object.
     *
     * @param caseType Case type class
     * @param childType the child type
     * @return All child modifications matching {@code childType} and {@code caseType}
     * @throws NullPointerException if any argument is {@code null}
     * @throws ClassCastException if any argument does not match its declared class bounds
     * @throws IllegalArgumentException if @code childType} class is not valid child according to generated model
     */
    public final <H extends ChoiceIn<? super T> & DataObject, C extends ChildOf<? super H>>
            @NonNull Collection<DataObjectModification<C>> getModifiedChildren(final @NonNull Class<H> caseType,
                final @NonNull Class<C> childType) {
        final var checkedCase = caseType.asSubclass(ChoiceIn.class).asSubclass(DataObject.class);
        final var checkedChild = childType.asSubclass(ChildOf.class);
        @SuppressWarnings("unchecked")
        final var ret = (@NonNull Collection<DataObjectModification<C>>) modifiedChildren().stream()
            .filter(child -> {
                final var childStep = child.step();
                return checkedChild.isAssignableFrom(childStep.type()) && checkedCase.equals(childStep.caseType());
            })
            .collect(Collectors.toUnmodifiableList());
        return ret;
    }

    /**
     * Returns container child modification if {@code child} was modified by this modification. This method should be
     * used if the child is defined in a grouping brought into a case inside this object.
     *
     * <p>For accessing all modified list items consider iterating over {@link #modifiedChildren()}.
     *
     * @param caseType Case type class
     * @param child Type of child - must be only container
     * @return Modification of {@code child} if {@code child} was modified, null otherwise.
     * @throws NullPointerException if any argument is {@code null}
     * @throws ClassCastException if any argument does not match its declared class bounds
     * @throws IllegalArgumentException if {@code child} class is not a valid child according to generated model
     */
    @SuppressWarnings("ReturnValueIgnored")
    public final <H extends ChoiceIn<? super T> & DataObject, C extends ChildOf<? super H>>
            @Nullable DataObjectModification<C> getModifiedChildContainer(final @NonNull Class<H> caseType,
                final @NonNull Class<C> child) {
        caseType.asSubclass(ChoiceIn.class).asSubclass(DataObject.class);
        child.asSubclass(ChildOf.class);
        return modifiedChild(new NodeStep<>(child, caseType));
    }

    /**
     * Returns container child modification if {@code child} was modified by this modification.
     *
     * <p>For accessing all modified list items consider iterating over {@link #modifiedChildren()}.
     *
     * @param child Type of child - must be only container
     * @return Modification of {@code child} if {@code child} was modified, null otherwise.
     * @throws NullPointerException if {@code child} is {@code null}
     * @throws ClassCastException if {@code child} is not actually a subclass of {@link ChildOf}
     * @throws IllegalArgumentException if {@code child} class is not a valid child according to generated model
     */
    @SuppressWarnings("ReturnValueIgnored")
    public final <C extends ChildOf<? super T>> @Nullable DataObjectModification<C> getModifiedChildContainer(
            final @NonNull Class<C> child) {
        child.asSubclass(ChildOf.class);
        return modifiedChild(new NodeStep<>(child));
    }

    /**
     * Returns augmentation child modification if {@code augmentation} was modified by this modification.
     *
     * <p>For accessing all modified list items consider iterating over {@link #modifiedChildren()}.
     *
     * @param augmentation Type of augmentation - must be only container
     * @return Modification of {@code augmentation} if {@code augmentation} was modified, null otherwise.
     * @throws NullPointerException if {@code augmentation} is {@code null}
     * @throws ClassCastException if {@code augmentation} is not actually a subclass of {@link Augmentation}
     * @throws IllegalArgumentException if @code augmentation} class is not a valid augmentation according to generated
     *                                  model
     */
    @SuppressWarnings("ReturnValueIgnored")
    public final <C extends Augmentation<T> & DataObject> @Nullable DataObjectModification<C> getModifiedAugmentation(
            final @NonNull Class<C> augmentation) {
        augmentation.asSubclass(Augmentation.class);
        return modifiedChild(new NodeStep<>(augmentation));
    }

    /**
     * Returns child list item modification if {@code child} was modified by this modification.
     *
     * @param listItem Type of list item - must be list item with key
     * @param listKey List item key
     * @return Modification of {@code child} if {@code child} was modified, null otherwise.
     * @throws NullPointerException if any argument is {@code null}
     * @throws ClassCastException if any argument does not match its declared class bounds
     * @throws IllegalArgumentException if @code listItem} is not a valid child according to generated model
     */
    @SuppressWarnings("ReturnValueIgnored")
    public final <N extends EntryObject<N, K> & ChildOf<? super T>, K extends Key<N>>
            @Nullable DataObjectModification<N> getModifiedChildListItem(final @NonNull Class<N> listItem,
                final @NonNull K listKey) {
        listItem.asSubclass(EntryObject.class).asSubclass(ChildOf.class);
        return modifiedChild(new KeyStep<>(listItem, listKey));
    }

    /**
     * Returns child list item modification if {@code child} was modified by this modification.
     *
     * @param listItem Type of list item - must be list item with key
     * @param listKey List item key
     * @return Modification of {@code child} if {@code child} was modified, null otherwise.
     * @throws NullPointerException if any argument is {@code null}
     * @throws ClassCastException if any argument does not match its declared class bounds
     * @throws IllegalArgumentException if {@code listItem} class is not a valid child according to generated model
     */
    @SuppressWarnings("ReturnValueIgnored")
    public final <H extends ChoiceIn<? super T> & DataObject, C extends EntryObject<C, K> & ChildOf<? super H>,
            K extends Key<C>> @Nullable DataObjectModification<C> getModifiedChildListItem(
                final @NonNull Class<H> caseType, final @NonNull Class<C> listItem, final @NonNull K listKey) {
        caseType.asSubclass(ChoiceIn.class).asSubclass(DataObject.class);
        listItem.asSubclass(EntryObject.class).asSubclass(ChildOf.class);
        return modifiedChild(new KeyStep<>(listItem, caseType, listKey));
    }

    /**
     * Returns a child modification if a node identified by {@code childArgument} was modified by this modification.
     *
     * @param childStep {@link ExactDataObjectStep} of child node
     * @return Modification of child identified by {@code childArgument} if {@code childArgument} was modified,
     *         {@code null} otherwise
     * @throws IllegalArgumentException If supplied step is not valid child according to generated model
     * @deprecated Use {@link #modifiedChild(ExactDataObjectStep)} instead
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    public final <C extends DataObject> @Nullable DataObjectModification<C> getModifiedChild(
            final @NonNull ExactDataObjectStep<C> childStep) {
        return modifiedChild(childStep);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    @NonNullByDefault
    protected abstract ToStringHelper addToStringAttributes(ToStringHelper helper);
}
