/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.ChoiceIn;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyAware;
import org.opendaylight.yangtools.yang.binding.ExactDataObjectStep;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Modified Data Object. Represents a modification of DataObject, which has a few kinds as indicated by
 * {@link #getModificationType()}.
 *
 * @param <T> Type of modified object
 */
public interface DataObjectModification<T extends DataObject> {
    /**
     * Represents type of modification which has occurred.
     */
    enum ModificationType {
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

    @Deprecated(since = "13.0.0", forRemoval = true)
    default @NonNull ExactDataObjectStep<T> getIdentifier() {
        return step();
    }

    /**
     * Return the {@link InstanceIdentifier} step this modification corresponds to.
     *
     * @return the {@link InstanceIdentifier} step this modification corresponds to
     */
    @NonNull ExactDataObjectStep<T> step();

    /**
     * Returns type of modified object.
     *
     * @return type of modified object.
     */
    default @NonNull Class<T> dataType() {
        return step().type();
    }

    /**
     * Returns type of modified object.
     *
     * @return type of modified object.
     * @deprecated Use {@link #dataType()} instead.
     */
    @Deprecated(since = "13.0.0", forRemoval = true)
    default @NonNull Class<T> getDataType() {
        return dataType();
    }

    /**
     * Returns type of modification.
     *
     * @return type of performed modification.
     */
    @NonNull ModificationType modificationType();

    /**
     * Returns type of modification.
     *
     * @return type Type of performed modification.
     */
    @Deprecated(since = "13.0.0", forRemoval = true)
    default @NonNull ModificationType getModificationType() {
        return modificationType();
    }

    /**
     * Returns before-state of top level container. Implementations are encouraged, but not required to provide this
     *  state.
     *
     * @return State of object before modification. Null if subtree was not present, or the implementation cannot
     *         provide the state.
     */
    @Nullable T dataBefore();

    /**
     * Returns before-state of top level container. Implementations are encouraged, but not required to provide this
     *  state.
     *
     * @return State of object before modification. Null if subtree was not present, or the implementation cannot
     *         provide the state.
     * @deprecated Use {@link #dataBefore()} instead.
     */
    @Deprecated(since = "13.0.0", forRemoval = true)
    default @Nullable T getDataBefore() {
        return dataBefore();
    }

    /**
     * Returns after-state of top level container.
     *
     * @return State of object after modification. Null if subtree is not present.
     */
    @Nullable T dataAfter();

    /**
     * Returns after-state of top level container.
     *
     * @return State of object after modification. Null if subtree is not present.
     * @deprecated Use {@link #dataAfter()} instead.
     */
    @Deprecated(since = "13.0.0", forRemoval = true)
    default @Nullable T getDataAfter() {
        return dataAfter();
    }

    /**
     * Returns unmodifiable collection of modified direct children.
     *
     * @return unmodifiable collection of modified direct children.
     */
    @NonNull Collection<? extends DataObjectModification<? extends DataObject>> modifiedChildren();

    /**
     * Returns unmodifiable collection of modified direct children.
     *
     * @return unmodifiable collection of modified direct children.
     * @deprecated Use {@link #modifiedChildren()} instead.
     */
    @Deprecated(since = "13.0.0", forRemoval = true)
    default @NonNull Collection<? extends DataObjectModification<? extends DataObject>> getModifiedChildren() {
        return modifiedChildren();
    }

    /**
     * Returns child list item modification if {@code child} was modified by this modification.
     *
     * @param childType Type of list item - must be list item with key
     * @return Modification of {@code child} if {@code child} was modified, null otherwise.
     * @throws IllegalArgumentException If supplied {@code childType} class is not valid child according
     *         to generated model.
     */
    <C extends ChildOf<? super T>> Collection<DataObjectModification<C>> getModifiedChildren(
            @NonNull Class<C> childType);

    /**
     * Returns child list item modification if {@code child} was modified by this modification. This method should be
     * used if the child is defined in a grouping brought into a case inside this object.
     *
     * @param caseType Case type class
     * @param childType Type of list item - must be list item with key
     * @return Modification of {@code child} if {@code child} was modified, null otherwise.
     * @throws IllegalArgumentException If supplied {@code childType} class is not valid child according
     *         to generated model.
     */
    <H extends ChoiceIn<? super T> & DataObject, C extends ChildOf<? super H>> Collection<DataObjectModification<C>>
            getModifiedChildren(@NonNull Class<H> caseType, @NonNull Class<C> childType);

    /**
     * Returns container child modification if {@code child} was modified by this modification. This method should be
     * used if the child is defined in a grouping brought into a case inside this object.
     *
     * <p>
     * For accessing all modified list items consider iterating over {@link #getModifiedChildren()}.
     *
     * @param caseType Case type class
     * @param child Type of child - must be only container
     * @return Modification of {@code child} if {@code child} was modified, null otherwise.
     * @throws IllegalArgumentException If supplied {@code child} class is not valid child according
     *         to generated model.
     */
    <H extends ChoiceIn<? super T> & DataObject, C extends ChildOf<? super H>> @Nullable DataObjectModification<C>
            getModifiedChildContainer(@NonNull Class<H> caseType, @NonNull Class<C> child);

    /**
     * Returns container child modification if {@code child} was modified by this
     * modification.
     *
     * <p>
     * For accessing all modified list items consider iterating over {@link #getModifiedChildren()}.
     *
     * @param child Type of child - must be only container
     * @return Modification of {@code child} if {@code child} was modified, null otherwise.
     * @throws IllegalArgumentException If supplied {@code child} class is not valid child according
     *         to generated model.
     */
    <C extends ChildOf<? super T>> @Nullable DataObjectModification<C> getModifiedChildContainer(
            @NonNull Class<C> child);

    /**
     * Returns augmentation child modification if {@code augmentation} was modified by this modification.
     *
     * <p>
     * For accessing all modified list items consider iterating over {@link #getModifiedChildren()}.
     *
     * @param augmentation Type of augmentation - must be only container
     * @return Modification of {@code augmentation} if {@code augmentation} was modified, null otherwise.
     * @throws IllegalArgumentException If supplied {@code augmentation} class is not valid augmentation
     *         according to generated model.
     */
    <C extends Augmentation<T> & DataObject> @Nullable DataObjectModification<C> getModifiedAugmentation(
            @NonNull Class<C> augmentation);

    /**
     * Returns child list item modification if {@code child} was modified by this modification.
     *
     * @param listItem Type of list item - must be list item with key
     * @param listKey List item key
     * @return Modification of {@code child} if {@code child} was modified, null otherwise.
     * @throws IllegalArgumentException If supplied {@code listItem} class is not valid child according
     *         to generated model.
     */
    <N extends KeyAware<K> & ChildOf<? super T>, K extends Key<N>> @Nullable DataObjectModification<N>
            getModifiedChildListItem(@NonNull Class<N> listItem, @NonNull K listKey);

    /**
     * Returns child list item modification if {@code child} was modified by this modification.
     *
     * @param listItem Type of list item - must be list item with key
     * @param listKey List item key
     * @return Modification of {@code child} if {@code child} was modified, null otherwise.
     * @throws IllegalArgumentException If supplied {@code listItem} class is not valid child according
     *         to generated model.
     */
    <H extends ChoiceIn<? super T> & DataObject, C extends KeyAware<K> & ChildOf<? super H>,
            K extends Key<C>> @Nullable DataObjectModification<C> getModifiedChildListItem(
                    @NonNull Class<H> caseType, @NonNull Class<C> listItem, @NonNull K listKey);

    /**
     * Returns a child modification if a node identified by {@code childArgument} was modified by this modification.
     *
     * @param childArgument {@link ExactDataObjectStep} of child node
     * @return Modification of child identified by {@code childArgument} if {@code childArgument} was modified,
     *         {@code null} otherwise
     * @throws IllegalArgumentException If supplied step is not valid child according to generated model
     */
    @Nullable DataObjectModification<? extends DataObject> getModifiedChild(ExactDataObjectStep<?> childArgument);
}
