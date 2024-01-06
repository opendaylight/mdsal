/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
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
        var dataBased = domChildNode.dataAfter();
        if (dataBased == null) {
            dataBased = domChildNode.dataBefore();
        }
        return dataBased != null ? from(dataBased) : from(domChildNode.name());
    }

    private static BindingStructuralType from(final PathArgument arg) {
        if (arg instanceof NodeIdentifierWithPredicates) {
            return VISIBLE_CONTAINER;
        } else if (arg instanceof NodeWithValue) {
            return NOT_ADDRESSABLE;
        } else {
            return UNKNOWN;
        }
    }

    static BindingStructuralType from(final NormalizedNode data) {
        if (data instanceof LeafNode || data instanceof AnyxmlNode || data instanceof AnydataNode
            || data instanceof LeafSetNode || data instanceof LeafSetEntryNode
            || data instanceof UnkeyedListNode || data instanceof UnkeyedListEntryNode) {
            return NOT_ADDRESSABLE;
        } else if (data instanceof ContainerNode || data instanceof MapEntryNode) {
            return VISIBLE_CONTAINER;
        } else if (data instanceof MapNode) {
            return INVISIBLE_LIST;
        } else if (data instanceof ChoiceNode) {
            return INVISIBLE_CONTAINER;
        } else {
            return UNKNOWN;
        }
    }

    public static BindingStructuralType recursiveFrom(final DataTreeCandidateNode node) {
        final var type = BindingStructuralType.from(node);
        return switch (type) {
            case INVISIBLE_CONTAINER, INVISIBLE_LIST -> {
                // This node is invisible, try to resolve using a child node
                for (var child : node.childNodes()) {
                    final var childType = recursiveFrom(child);
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
}
