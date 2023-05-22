/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.AnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;

/**
 * Defines structural mapping of Normalized Node to Binding data addressable by Instance Identifier. Not all binding
 * data are addressable by instance identifier and there are some differences.
 *
 * <p>
 * See {@link #NOT_ADDRESSABLE},{@link #INVISIBLE_CONTAINER},{@link #VISIBLE_CONTAINER} for more details.
 *
 * <p>
 * NOTE: this class is exposed for migration purposes only, no new users outside of its package should be introduced.
 */
@Beta
public enum BindingStructuralType {
    /**
     * DOM Item is not addressable in Binding InstanceIdentifier, data is not lost, but are available only via parent
     * object. Such types of data are leaf-lists, leafs, list without keys or anyxml.
     */
    NOT_ADDRESSABLE,
    /**
     * Data container is addressable in NormalizedNode format, but in Binding it is not represented in
     * InstanceIdentifier. These are choice / case nodes.
     *
     * <p>
     * This data is still accessible using parent object and their children are addressable.
     */
    INVISIBLE_CONTAINER,
    /**
     * Data container is addressable in NormalizedNode format, but in Binding it is not represented in
     * InstanceIdentifier. These are list nodes.
     *
     * <p>
     * This data is still accessible using parent object and their children are addressable.
     */
    INVISIBLE_LIST,
    /**
     * Data container is addressable in Binding InstanceIdentifier format and also YangInstanceIdentifier format.
     */
    VISIBLE_CONTAINER,
    /**
     * Mapping algorithm was unable to detect type or was not updated after introduction of new NormalizedNode type.
     */
    UNKNOWN;

    public static BindingStructuralType from(final DataTreeCandidateNode domChildNode) {
        Optional<NormalizedNode> dataBased = domChildNode.getDataAfter();
        if (!dataBased.isPresent()) {
            dataBased = domChildNode.getDataBefore();
        }
        if (dataBased.isPresent()) {
            return from(dataBased.orElseThrow());
        }
        return from(domChildNode.getIdentifier());
    }

    private static BindingStructuralType from(final PathArgument identifier) {
        if (identifier instanceof NodeIdentifierWithPredicates) {
            return VISIBLE_CONTAINER;
        }
        if (identifier instanceof NodeWithValue) {
            return NOT_ADDRESSABLE;
        }
        return UNKNOWN;
    }

    static BindingStructuralType from(final NormalizedNode data) {
        if (isNotAddressable(data)) {
            return NOT_ADDRESSABLE;
        }
        if (data instanceof MapNode) {
            return INVISIBLE_LIST;
        }
        if (data instanceof ChoiceNode) {
            return INVISIBLE_CONTAINER;
        }
        if (isVisibleContainer(data)) {
            return VISIBLE_CONTAINER;
        }
        return UNKNOWN;
    }

    public static BindingStructuralType recursiveFrom(final DataTreeCandidateNode node) {
        final BindingStructuralType type = BindingStructuralType.from(node);
        return switch (type) {
            case INVISIBLE_CONTAINER, INVISIBLE_LIST -> {
                // This node is invisible, try to resolve using a child node
                for (final DataTreeCandidateNode child : node.getChildNodes()) {
                    final BindingStructuralType childType = recursiveFrom(child);
                    yield switch (childType) {
                            case INVISIBLE_CONTAINER, INVISIBLE_LIST ->
                                // Invisible nodes are not addressable
                                BindingStructuralType.NOT_ADDRESSABLE;
                            case NOT_ADDRESSABLE, UNKNOWN, VISIBLE_CONTAINER -> childType;
                        };
                }

                yield BindingStructuralType.NOT_ADDRESSABLE;
            }
            default -> type;
        };
    }

    private static boolean isVisibleContainer(final NormalizedNode data) {
        return data instanceof MapEntryNode || data instanceof ContainerNode;
    }

    private static boolean isNotAddressable(final NormalizedNode normalizedNode) {
        return normalizedNode instanceof LeafNode
                || normalizedNode instanceof AnyxmlNode
                || normalizedNode instanceof LeafSetNode
                || normalizedNode instanceof LeafSetEntryNode
                || normalizedNode instanceof UnkeyedListNode
                || normalizedNode instanceof UnkeyedListEntryNode;
    }
}
