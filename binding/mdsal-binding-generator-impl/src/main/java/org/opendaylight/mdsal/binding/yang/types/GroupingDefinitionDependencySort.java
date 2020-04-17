/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.util.TopologicalSort;
import org.opendaylight.yangtools.util.TopologicalSort.Node;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationNodeContainer;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

public class GroupingDefinitionDependencySort {
    /**
     * Sorts set {@code groupingDefinitions} according to the mutual dependencies.<br>
     * Elements of {@code groupingDefinitions} are firstly transformed to {@link Node} interfaces and then are
     * sorted by {@link TopologicalSort#sort(Set) sort()} method of {@code TopologicalSort}.<br>
     *
     * <i>Definition of dependency relation:<br>
     * The first {@code GroupingDefinition} object (in this context) depends on second {@code GroupingDefinition} object
     * if the first one contains in its set of {@code UsesNode} (obtained through {@link DataNodeContainer#getUses()})
     * a reference to the second one.
     * </i>
     *
     * @param groupingDefinitions set of grouping definition which should be sorted according to mutual dependencies
     * @return list of grouping definitions which are sorted by mutual dependencies
     * @throws IllegalArgumentException if {@code groupingDefinitions}
     *
     */
    public List<GroupingDefinition> sort(final Collection<? extends GroupingDefinition> groupingDefinitions) {
        if (groupingDefinitions == null) {
            throw new IllegalArgumentException("Set of Type Definitions cannot be NULL!");
        }

        final List<Node> sortedNodes = TopologicalSort.sort(groupingDefinitionsToNodes(groupingDefinitions));
        final List<GroupingDefinition> resultGroupingDefinitions = new ArrayList<>(sortedNodes.size());
        for (Node node : sortedNodes) {
            NodeWrappedType nodeWrappedType = (NodeWrappedType) node;
            resultGroupingDefinitions.add((GroupingDefinition) nodeWrappedType.getWrappedType());
        }
        return resultGroupingDefinitions;
    }

    /**
     * Wraps every grouping definition to node type and adds to every node information about dependencies. The map
     * with mapping from schema path (represents grouping definition) to node is created. For every created node
     * (next <i>nodeFrom</i>) is for its wrapped grouping definition passed the set of its <i>uses nodes</i> through.
     * For every uses node is found its wrapping node (next as <i>nodeTo</i>). This dependency relationship between
     * nodeFrom and all found nodesTo is modeled with creating of one edge from nodeFrom to nodeTo.
     *
     * @param groupingDefinitions set of grouping definition which will be wrapped to nodes
     * @return set of nodes where every one contains wrapped grouping definition
     */
    private Set<Node> groupingDefinitionsToNodes(final Collection<? extends GroupingDefinition> groupingDefinitions) {
        final Map<GroupingDefinition, Node> nodeMap = new HashMap<>();
        final Set<Node> resultNodes = new HashSet<>();

        for (final GroupingDefinition groupingDefinition : groupingDefinitions) {
            final Node node = new NodeWrappedType(groupingDefinition);
            nodeMap.put(groupingDefinition, node);
            resultNodes.add(node);
        }

        for (final Node node : resultNodes) {
            final NodeWrappedType nodeWrappedType = (NodeWrappedType) node;
            final GroupingDefinition groupingDefinition = (GroupingDefinition) nodeWrappedType.getWrappedType();

            Set<UsesNode> usesNodes = getAllUsesNodes(groupingDefinition);

            for (UsesNode usesNode : usesNodes) {
                Node nodeTo = nodeMap.get(usesNode.getSourceGrouping());
                if (nodeTo != null) {
                    nodeWrappedType.addEdge(nodeTo);
                }
            }
        }

        return resultNodes;
    }

    /**
     * Returns the set of the uses nodes which are get from uses in <code>container</code>, from uses in groupings
     * inside <code>container</code> and from uses inside child nodes of the <code>container</code>.
     *
     * @param container data node container which can contain some uses of grouping
     * @return set of uses nodes which were find in <code>container</code>.
     */
    private Set<UsesNode> getAllUsesNodes(final DataNodeContainer container) {
        Set<UsesNode> ret = new HashSet<>();
        Collection<? extends UsesNode> usesNodes = container.getUses();
        ret.addAll(usesNodes);

        for (UsesNode usesNode : usesNodes) {
            for (AugmentationSchemaNode augment : usesNode.getAugmentations()) {
                ret.addAll(getAllUsesNodes(augment));
            }
        }
        for (GroupingDefinition groupingDefinition : container.getGroupings()) {
            ret.addAll(getAllUsesNodes(groupingDefinition));
        }
        for (DataSchemaNode childNode : container.getChildNodes()) {
            if (childNode instanceof DataNodeContainer) {
                ret.addAll(getAllUsesNodes((DataNodeContainer) childNode));
            } else if (childNode instanceof ChoiceSchemaNode) {
                for (CaseSchemaNode choiceCaseNode : ((ChoiceSchemaNode) childNode).getCases()) {
                    ret.addAll(getAllUsesNodes(choiceCaseNode));
                }
            }
        }
        if (container instanceof ActionNodeContainer) {
            for (ActionDefinition action : ((ActionNodeContainer) container).getActions()) {
                ret.addAll(getAllUsesNodes(action.getInput()));
                ret.addAll(getAllUsesNodes(action.getOutput()));
            }
        }
        if (container instanceof NotificationNodeContainer) {
            for (NotificationDefinition notification : ((NotificationNodeContainer) container).getNotifications()) {
                ret.addAll(getAllUsesNodes(notification));
            }
        }

        return ret;
    }
}
