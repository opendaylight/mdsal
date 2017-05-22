/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.spec.base.IdentifiableItem;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.mdsal.binding.javav2.spec.structural.TreeChildNode;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

/**
 * Modified Tree Node.
 *
 *<p>
 * Represents modification of tree node {@link TreeNode}
 *
 *<p>
 * @param <T> Type of modified object
 *
 */
@Beta
public interface TreeNodeModification<T extends TreeNode> extends Identifiable<TreeArgument> {

    /**
     * Represents type of modification which has occured.
     *
     */
    enum ModificationType {
        /**
         * Child node (direct or indirect) was modified.
         *
         */
        SUBTREE_MODIFIED,
        /**
         * Node was explicitly created / overwritten.
         *
         */
        WRITE,
        /**
         * Node was deleted.
         *
         */
        DELETE,
        /**
         * This node has appeared because it is implied by one of its children. This type is usually produced when a
         * structural container is created to host some leaf entries. It does not have an associated before-image.
         * Its semantics is a combination of SUBTREE_MODIFIED and WRITE, depending on which context it is being
         * interpreted.
         * Users who track the value of the node can treat it as a WRITE. Users transforming a {@link DataTreeCandidate}
         * to operations on a {@link org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification}
         * should interpret it as a SUBTREE_MODIFIED and examine its children.
         * This is needed to correctly deal with concurrent operations on the nodes children, as issuing a write on the
         * DataTreeModification could end up removing any leaves which have not been present at the DataTree which
         * emitted this event.
         */
        APPEARED,
        /**
         * This node has disappeared because it is no longer implied by any children. This type is usually produced when
         * a structural container is removed because it has become empty. It does not have an associated after-image.
         * Its semantics is a combination of SUBTREE_MODIFIED and DELETE, depending on which context it is being
         * interpreted.
         * Users who track the value of the node can treat it as a DELETE, as the container has disappeared.
         * Users transforming a {@link DataTreeCandidate} to operations on a
         * {@link org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification} should interpret
         * it as a SUBTREE_MODIFIED and examine its children.
         * This is needed to correctly deal with concurrent operations on the nodes children, as issuing a delete on the
         * DataTreeModification would end up removing any leaves which have not been present at the DataTree which
         * emitted this event.
         */
        DISAPPEARED
    }

    @Nonnull
    @Override
    TreeArgument getIdentifier();

    /**
     * Returns type of modified object.
     *
     * @return type of modified object.
     */
    @Nonnull Class<T> getDataType();

    /**
     * Returns type of modification.
     *
     * @return type Type of performed modification.
     */
    @Nonnull ModificationType getModificationType();

    /**
     * Returns before-state of top level container. Implementations are encouraged, but not required
     * to provide this state.
     *
     * @return State of object before modification. Null if subtree was not present, or the
     *         implementation cannot provide the state.
     */
    @Nullable
    T getDataBefore();

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
    @Nonnull
    Collection<TreeNodeModification<? extends TreeNode>> getModifiedChildren();

    /**
     * Returns child list item modification if {@code child} was modified by this modification.
     *
     * @param childType Type of list item - must be list item with key
     * @param <C> type of child class
     * @return Modification of {@code child} if {@code child} was modified, null otherwise.
     * @throws IllegalArgumentException If supplied {@code childType} class is not valid child according
     *         to generated model.
     */
    <C extends TreeChildNode<? super T, ?>> Collection<TreeNodeModification<C>> getModifiedChildren(
            @Nonnull Class<C> childType);

    /**
     * Returns container child modification if {@code child} was modified by this
     * modification.
     *
     *<p>
     * For accessing all modified list items consider iterating over {@link #getModifiedChildren()}.
     *
     * @param child Type of child - must be only container
     * @param <C> type of child class
     * @return Modification of {@code child} if {@code child} was modified, null otherwise.
     * @throws IllegalArgumentException If supplied {@code child} class is not valid child according
     *         to generated model.
     */
    @Nullable <C extends TreeChildNode<? super T, ?>> TreeNodeModification<C> getModifiedChildContainer(
            @Nonnull Class<C> child);

    /**
     * Returns augmentation child modification if {@code augmentation} was modified by this
     * modification.
     *
     *<p>
     * For accessing all modified list items consider iterating over {@link #getModifiedChildren()}.
     *
     * @param augmentation Type of augmentation - must be only container
     * @param <C> type of augmentation class
     * @return Modification of {@code augmentation} if {@code augmentation} was modified, null otherwise.
     * @throws IllegalArgumentException If supplied {@code augmentation} class is not valid augmentation
     *         according to generated model.
     */
    @Nullable <C extends Augmentation<T> & TreeNode> TreeNodeModification<C> getModifiedAugmentation(
            @Nonnull Class<C> augmentation);


    /**
     * Returns child list item modification if {@code child} was modified by this modification.
     *
     * @param listItem Type of list item - must be list item with key
     * @param listKey List item key
     * @param <C> type of list item class
     * @param <K> type of list key
     * @return Modification of {@code child} if {@code child} was modified, null otherwise.
     * @throws IllegalArgumentException If supplied {@code listItem} class is not valid child according
     *         to generated model.
     */
    <C extends IdentifiableItem<T, K> & TreeChildNode<? super T, ?>, K extends IdentifiableItem<T, K>>
        TreeNodeModification<C> getModifiedChildListItem(@Nonnull Class<C> listItem, @Nonnull K listKey);

    /**
     * Returns a child modification if a node identified by {@code childArgument} was modified by
     * this modification.
     *
     * @param childArgument Path Argument of child node
     * @return Modification of child identified by {@code childArgument} if {@code childArgument}
     *         was modified, null otherwise.
     * @throws IllegalArgumentException If supplied path argument is not valid child according to
     *         generated model.
     *
     */
    @Nullable
    TreeNodeModification<? extends TreeNode> getModifiedChild(TreeArgument childArgument);

}
