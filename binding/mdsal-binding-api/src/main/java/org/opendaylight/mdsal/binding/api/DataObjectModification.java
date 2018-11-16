/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;

/**
 * Modified Data Object. Represents a modification of DataObject, which has a few kinds as indicated by
 * {@link #getModificationType()}.
 *
 * @param <T> Type of modified object
 */
public interface DataObjectModification<T extends DataObject> extends
        org.opendaylight.yangtools.concepts.Identifiable<PathArgument> {
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

    @Override
    PathArgument getIdentifier();

    /**
     * Returns type of modified object.
     *
     * @return type of modified object.
     */
    @NonNull Class<T> getDataType();

    @SuppressWarnings("unchecked")
    default <N extends DataObject> DataObjectModification<N> safeCast(final Class<N> newType) {
        final Class<T> dataType = getDataType();
        checkArgument(newType.isAssignableFrom(dataType),
            "Cannot reinterpret modification of %s as a modification of %s", dataType, newType);
        return (DataObjectModification<N>) this;
    }

    /**
     * Returns type of modification.
     *
     * @return type Type of performed modification.
     */
    @NonNull ModificationType getModificationType();

    /**
     * Returns before-state of top level container. Implementations are encouraged, but not required
     * to provide this state.
     *
     * @return State of object before modification. Null if subtree was not present, or the
     *         implementation cannot provide the state.
     */
    @Nullable T getDataBefore();

    /**
     * Returns after-state of top level container.
     *
     * @return State of object after modification. Null if subtree is not present.
     */
    @Nullable T getDataAfter();

    /**
     * Returns unmodifiable collection of modified direct children.
     *
     * @return unmodifiable collection of modified direct children.
     */
    @NonNull Collection<? extends DataObjectModification<? extends DataObject>> getModifiedChildren();

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
    <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>> @Nullable DataObjectModification<N>
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
    <H extends ChoiceIn<? super T> & DataObject, C extends Identifiable<K> & ChildOf<? super H>,
            K extends Identifier<C>> @Nullable DataObjectModification<C> getModifiedChildListItem(
                    @NonNull Class<H> caseType, @NonNull Class<C> listItem, @NonNull K listKey);

    /**
     * Returns a child modification if a node identified by {@code childArgument} was modified by this modification.
     *
     * @param childArgument Path Argument of child node
     * @return Modification of child identified by {@code childArgument} if {@code childArgument}
     *         was modified, null otherwise.
     * @throws IllegalArgumentException If supplied path argument is not valid child according to
     *         generated model.
     */
    @Nullable DataObjectModification<? extends DataObject> getModifiedChild(PathArgument childArgument);
}
