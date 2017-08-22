/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.spec.runtime;

import com.google.common.annotations.Beta;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import org.opendaylight.mdsal.binding.javav2.spec.base.IdentifiableItem;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.yangtools.concepts.Identifiable;

/**
 * Event Stream Writer for Binding version 2 Representation
 *
 *
 * <h3>Emitting Event Stream</h3>
 *
 * <ul>
 * <li><code>container</code> - Container node representation, start event is
 * emitted using {@link #startContainerNode(Class, int)} and node end event is
 * emitted using {@link #endNode()}. Container node is implementing
 * {@link TreeNode} interface.
 *
 * <li><code>list</code> - YANG list statement has two representations in event
 * stream - un-keyed list and map. Un-keyed list is YANG list which didn't
 * specify key.
 *
 * <ul>
 * <li><code>Map</code> - Map start event is emitted using
 * {@link #startMapNode(Class, int)} and is ended using {@link #endNode()}. Each map
 * entry start is emitted using {@link #startMapEntryNode(IdentifiableItem, int)} with Map of keys
 * and finished using {@link #endNode()}.</li>
 *
 * <li><code>UnkeyedList</code> - Un-keyed list represents list without keys,
 * un-keyed list start is emitted using {@link #startUnkeyedList(Class, int)}, list
 * end is emitted using {@link #endNode()}. Each list item is emitted using
 * {@link #startUnkeyedListItem(int)} and ended using {@link #endNode()}.</li>
 * </ul></li>
 *
 * <li><code>leaf</code> - Leaf node event is emitted using
 * {@link #leafNode(String, Object)}. {@link #endNode()} MUST NOT be emitted for
 * leaf node.</li>
 *
 * <li><code>leaf-list</code> - Leaf list start is emitted using
 * {@link #startLeafSet(String, int)}. Leaf list end is emitted using
 * {@link #endNode()}. Leaf list entries are emitted using
 * {@link #leafSetEntryNode(Object)}.
 *
 * <li><code>anyxml - Anyxml node event is emitted using
 * {@link #startAnyxmlNode(String, Object)}. {@link #endNode()} MUST NOT be emitted
 * for anyxml node.</code></li>
 *
 * <li><code>anydata - Anydata node event is emitted using
 * {@link #startAnydataNode(String, Object)}. {@link #endNode()} MUST NOT be emitted
 * for anydata node.</code></li>
 *
 * <li><code>choice</code> Choice node event is emitted by
 * {@link #startChoiceNode(Item, int)} event and must be immediately followed by
 * {@link #startCase(Class, int)} event. Choice node is finished by emitting an
 * {@link #endNode()} event.</li>
 *
 * <li>
 * <code>case</code> - Case node may be emitted only inside choice node by
 * invoking {@link #startCase(Class, int)}. Case node is finished be emitting an
 * {@link #endNode()} event.</li>
 *
 * <li>
 * <code>augment</code> - Represents augmentation, augmentation node is started
 * by invoking {@link #startAugmentationNode(Class)} and
 * finished by invoking {@link #endNode()}.</li>
 *
 * </ul>
 *
 * <h3>Implementation notes</h3> This interface is not intended to be
 * implemented by users of generated Binding2 DTOs but to be used by utilities,
 * which needs to emit NormalizedNode model from Binding2 DTOs.
 * <p>
 * This interface is intended as API definition of facade for real Event /
 * Stream Writer, without explicitly requiring stream writer and related
 * interfaces to be imported by all generated Binding2 DTOs.
 * <p>
 * Existence of this interface in runtime Java Binding2 package is required to
 * support runtime generation of users of this interface in OSGI and OSGI-like
 * environment, since this package is only package which is imported by all
 * generated Binding2 DTOs and wired in OSGI.
 *
 *
 */
@Beta
public interface BindingStreamEventWriter extends Closeable, Flushable {

    /**
     * Methods in this interface allow users to hint the underlying
     * implementation about the sizing of container-like constructors
     * (leafLists, containers, etc.). These hints may be taken into account by a
     * particular implementation to improve performance, but clients are not
     * required to provide hints. This constant should be used by clients who
     * either do not have the sizing information, or do not wish to divulge it
     * (for whatever reasons). Implementations are free to ignore these hints
     * completely, but if they do use them, they are expected to be resilient in
     * face of missing and mismatched hints, which is to say the user can
     * specify startLeafSet(..., 1) and then call leafNode() 15 times.
     * <p>
     * The acceptable hint values are non-negative integers and this constant,
     * all other values will result, based on implementation preference, in the
     * hint being completely ignored or IllegalArgumentException being thrown.
     */
    int UNKNOWN_SIZE = -1;

    /**
     *
     * Emits a leaf node event with supplied value.
     *
     * @param localName
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @param value
     *            Value of leaf node.
     * @throws IllegalArgumentException
     *             If emitted leaf node has invalid value in current context or
     *             was emitted multiple times.
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     * @throws IOException if an underlying IO error occurs
     */
    void leafNode(String localName, Object value) throws IOException;

    /**
     *
     * Emits a start of leaf set (leaf-list).
     * <p>
     * Emits start of leaf set, during writing leaf set event, only
     * {@link #leafSetEntryNode(Object)} calls are valid. Leaf set event is
     * finished by calling {@link #endNode()}.
     *
     * @param localName
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @param childSizeHint
     *            Non-negative count of expected direct child nodes or
     *            {@link #UNKNOWN_SIZE} if count is unknown. This is only hint
     *            and should not fail writing of child events, if there are more
     *            events than count.
     * @throws IllegalArgumentException
     *             If emitted leaf node is invalid in current context or was
     *             emitted multiple times.
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     * @throws IOException if an underlying IO error occurs
     */
    void startLeafSet(String localName, int childSizeHint) throws IOException;

    /**
     *
     * Emits a start of leaf set (leaf-list).
     * <p>
     * Emits start of leaf set, during writing leaf set event, only
     * {@link #leafSetEntryNode(Object)} calls are valid. Leaf set event is
     * finished by calling {@link #endNode()}.
     *
     * @param localName
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @param childSizeHint
     *            Non-negative count of expected direct child nodes or
     *            {@link #UNKNOWN_SIZE} if count is unknown. This is only hint
     *            and should not fail writing of child events, if there are more
     *            events than count.
     * @throws IllegalArgumentException
     *             If emitted leaf node is invalid in current context or was
     *             emitted multiple times.
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     * @throws IOException if an underlying IO error occurs
     */
    void startOrderedLeafSet(String localName, int childSizeHint) throws IOException;

    /**
     * Emits a leaf set entry node
     *
     * @param value
     *            Value of leaf set entry node.
     * @throws IllegalArgumentException
     *             If emitted leaf node has invalid value.
     * @throws IllegalStateException
     *             If node was emitted outside <code>leaf set</code> node.
     * @throws IOException if an underlying IO error occurs
     */
    void leafSetEntryNode(Object value) throws IOException;

    /**
     *
     * Emits start of new container.
     *
     * <p>
     * End of container event is emitted by invoking {@link #endNode()}.
     *
     * <p>
     * Valid sub-events are:
     * <ul>
     * <li>{@link #leafNode(String, Object)}</li>
     * <li>{@link #startContainerNode(Class, int)}</li>
     * <li>{@link #startChoiceNode(Item, int)}</li>
     * <li>{@link #startLeafSet(String, int)}</li>
     * <li>{@link #startMapNode(Class, int)}</li>
     * <li>{@link #startUnkeyedList(Class, int)}</li>
     * <li>{@link #startAugmentationNode(Class)}</li>
     * </ul>
     *
     * @param container
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @param childSizeHint
     *            Non-negative count of expected direct child nodes or
     *            {@link #UNKNOWN_SIZE} if count is unknown. This is only hint
     *            and should not fail writing of child events, if there are more
     *            events than count.
     * @throws IllegalArgumentException
     *             If emitted node is invalid in current context or was emitted
     *             multiple times.
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     * @throws IOException if an underlying IO error occurs
     */
    void startContainerNode(Class<? extends TreeNode> container, int childSizeHint) throws IOException;

    /**
     *
     * Emits start of unkeyed list node event.
     *
     * <p>
     * End of unkeyed list event is emitted by invoking {@link #endNode()}.
     * Valid sub-event is only {@link #startUnkeyedListItem(int)}. All other
     * methods will throw {@link IllegalArgumentException}.
     *
     * @param localName
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @param childSizeHint
     *            Non-negative count of expected direct child nodes or
     *            {@link #UNKNOWN_SIZE} if count is unknown. This is only hint
     *            and should not fail writing of child events, if there are more
     *            events than count.
     * @throws IllegalArgumentException
     *             If emitted node is invalid in current context or was emitted
     *             multiple times.
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     * @throws IOException if an underlying IO error occurs
     */
    void startUnkeyedList(Class<? extends TreeNode> localName, int childSizeHint) throws IOException;

    /**
     * Emits start of new unkeyed list item.
     *
     * <p>
     * Un-keyed list item event is finished by invoking {@link #endNode()}.
     * <p>
     * Valid sub-events are:
     *
     * <ul>
     * <li>{@link #leafNode(String, Object)}</li>
     * <li>{@link #startContainerNode(Class, int)}</li>
     * <li>{@link #startChoiceNode(Item, int)}</li>
     * <li>{@link #startLeafSet(String, int)}</li>
     * <li>{@link #startMapNode(Class, int)}</li>
     * <li>{@link #startUnkeyedList(Class, int)}</li>
     * <li>{@link #startAugmentationNode(Class)}</li>
     * </ul>
     *
     *
     * @param childSizeHint
     *            Non-negative count of expected direct child nodes or
     *            {@link #UNKNOWN_SIZE} if count is unknown. This is only hint
     *            and should not fail writing of child events, if there are more
     *            events than count.
     * @throws IllegalStateException
     *             If node was emitted outside <code>unkeyed list</code> node.
     * @throws IOException if an underlying IO error occurs
     */
    void startUnkeyedListItem(int childSizeHint) throws IOException;

    /**
     *
     * Emits start of unordered map node event.
     *
     * <p>
     * End of map node event is emitted by invoking {@link #endNode()}. Valid
     * subevents is only {@link #startMapEntryNode(IdentifiableItem, int)}. All other methods will
     * throw {@link IllegalArgumentException}.
     *
     * @param mapEntryType
     *            Class of list.
     * @param childSizeHint
     *            Non-negative count of expected direct child nodes or
     *            {@link #UNKNOWN_SIZE} if count is unknown. This is only hint
     *            and should not fail writing of child events, if there are more
     *            events than count.
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     * @throws IOException if an underlying IO error occurs
     */
     <T extends TreeNode & Identifiable<?>> void startMapNode(Class<T> mapEntryType, int childSizeHint)
            throws IOException;

    /**
     *
     * Emits start of ordered map node event.
     *
     * <p>
     * End of map node event is emitted by invoking {@link #endNode()}. Valid
     * sub-event is only {@link #startMapEntryNode(IdentifiableItem, int)}. All other methods will
     * throw {@link IllegalArgumentException}.
     *
     * @param mapEntryType
     *            Class of list item, which has defined key.
     * @param childSizeHint
     *            Non-negative count of expected direct child nodes or
     *            {@link #UNKNOWN_SIZE} if count is unknown. This is only hint
     *            and should not fail writing of child events, if there are more
     *            events than count.
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     * @throws IOException if an underlying IO error occurs
     */
    <T extends TreeNode & Identifiable<?>> void startOrderedMapNode(Class<T> mapEntryType, int childSizeHint)
            throws IOException;

    /**
     *
     * Emits start of map entry.
     *
     * <p>
     * End of map entry event is emitted by invoking {@link #endNode()}.
     *
     * <p>
     * Valid sub-events are:
     * <ul>
     * <li>{@link #leafNode(String, Object)}</li>
     * <li>{@link #startContainerNode(Class, int)}</li>
     * <li>{@link #startChoiceNode(Item, int)}</li>
     * <li>{@link #startLeafSet(String, int)}</li>
     * <li>{@link #startMapNode(Class, int)}</li>
     * <li>{@link #startUnkeyedList(Class, int)}</li>
     * <li>{@link #startAugmentationNode(Class)}</li>
     * </ul>
     *
     * @param keyValues
     *            Key of map entry node
     * @param childSizeHint
     *            Non-negative count of expected direct child nodes or
     *            {@link #UNKNOWN_SIZE} if count is unknown. This is only hint
     *            and should not fail writing of child events, if there are more
     *            events than count.
     * @throws IllegalArgumentException
     *             If key contains incorrect value.
     * @throws IllegalStateException
     *             If node was emitted outside <code>map entry</code> node.
     * @throws IOException if an underlying IO error occurs
     */
    <I extends TreeNode, T> void startMapEntryNode(IdentifiableItem<I, T> keyValues, int childSizeHint)
            throws IOException;

    /**
     * Emits start of choice node.
     *
     * <p>
     * Valid sub-event is {@link #startCase(Class, int)}, which selects case
     * which should be written.
     *
     * @param choice
     *            Choice class.
     * @param childSizeHint
     *            Non-negative count of expected direct child nodes or
     *            {@link #UNKNOWN_SIZE} if count is unknown. This is only hint
     *            and should not fail writing of child events, if there are more
     *            events than count.
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>, <code>choice</code>,
     *             <code>unkeyed list</code> node.
     * @throws IOException if an underlying IO error occurs
     */
    <T extends TreeNode> void startChoiceNode(Item<T> choice, int childSizeHint) throws IOException;

    /**
     *
     * Starts a case node.
     *
     * <p>
     * Valid sub-events are:
     * <ul>
     * <li>{@link #leafNode(String, Object)}</li>
     * <li>{@link #startContainerNode(Class, int)}</li>
     * <li>{@link #startChoiceNode(Item, int)}</li>
     * <li>{@link #startLeafSet(String, int)}</li>
     * <li>{@link #startMapNode(Class, int)}</li>
     * <li>{@link #startUnkeyedList(Class, int)}</li>
     * <li>{@link #startAugmentationNode(Class)}</li>
     * </ul>
     *
     * @param caze Case class
     * @throws IllegalArgumentException
     * @throws IOException if an underlying IO error occurs
     */
    void startCase(Class<? extends TreeNode> caze, int childSizeHint) throws IOException;

    /**
     * Emits start of augmentation node.
     *
     * <p>
     * End of augmentation event is emitted by invoking {@link #endNode()}.
     *
     * <p>
     * Valid sub-events are:
     *
     * <ul>
     * <li>{@link #leafNode(String, Object)}</li>
     * <li>{@link #startContainerNode(Class, int)}</li>
     * <li>{@link #startChoiceNode(Item, int)}</li>
     * <li>{@link #startLeafSet(String, int)}</li>
     * <li>{@link #startMapNode(Class, int)}</li>
     * <li>{@link #startUnkeyedList(Class, int)}</li>
     * </ul>
     *
     * <p>
     * Note this is only method, which does not require childSizeHint, since
     * maximum value is always size of <code>possibleChildren</code>.
     *
     * @param augmentationType augmentation class
     * @throws IllegalArgumentException
     *             If augmentation is invalid in current context.
     * @throws IOException if an underlying IO error occurs
     */
    void startAugmentationNode(Class<? extends Augmentation<?>> augmentationType) throws IOException;

    /**
     * Emits anyxml node event.
     *
     * @param name
     * @param value
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     * @throws IOException if an underlying IO error occurs
     */
    void startAnyxmlNode(String name, Object value) throws IOException;

    /**
     * Emits anydata node event.
     *
     * @param name
     * @param value
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     * @throws IOException if an underlying IO error occurs
     */
    void startAnydataNode(String name, Object value) throws IOException;

    /**
     * Emits end event for node.
     *
     * @throws IllegalStateException If there is no open node.
     * @throws IOException if an underlying IO error occurs
     */
    void endNode() throws IOException;

    @Override
    void flush() throws IOException;

    @Override
    void close() throws IOException;
}

