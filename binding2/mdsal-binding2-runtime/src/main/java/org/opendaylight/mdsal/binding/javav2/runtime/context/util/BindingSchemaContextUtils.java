/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.runtime.context.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Iterator;
import java.util.Set;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.YangModuleInfo;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.mdsal.binding.javav2.spec.structural.TreeChildNode;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;

@Beta
public final class BindingSchemaContextUtils {

    private BindingSchemaContextUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Find data node container by binding path in schema context.
     *
     * FIXME: This method does not search in case augmentations.
     *
     * @param ctx
     *            - schema context
     * @param path
     *            - binding path
     * @return node container by binding path if exists, absent otherwise
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Optional<DataNodeContainer> findDataNodeContainer(final SchemaContext ctx,
            final InstanceIdentifier<?> path) {
        final Iterator<TreeArgument> pathArguments = path.getPathArguments().iterator();
        TreeArgument currentArg = pathArguments.next();
        Preconditions.checkArgument(currentArg != null);
        QName currentQName = BindingReflections.findQName(currentArg.getType());

        Optional<DataNodeContainer> currentContainer;
        if (BindingReflections.isNotification(currentArg.getType())) {
            currentContainer = findNotification(ctx, currentQName);
        } else if (BindingReflections.isOperationType(currentArg.getType())) {
            currentContainer = findFirstDataNodeContainerInRpcOrAction(ctx, currentArg.getType());
            if(currentQName == null && currentContainer.isPresent()) {
                currentQName = ((DataSchemaNode) currentContainer.get()).getQName();
            }
        } else {
            currentContainer = findDataNodeContainer(ctx, currentQName);
        }

        while (currentContainer.isPresent() && pathArguments.hasNext()) {
            currentArg = pathArguments.next();
            if (Augmentation.class.isAssignableFrom(currentArg.getType())) {
                currentQName = BindingReflections.findQName(currentArg.getType());
                if(pathArguments.hasNext()) {
                    currentArg = pathArguments.next();
                } else {
                    return currentContainer;
                }
            }
            if (TreeChildNode.class.isAssignableFrom(currentArg.getType())
                    && BindingReflections.isAugmentationChild(currentArg.getType())) {
                currentQName = BindingReflections.findQName(currentArg.getType());
            } else {
                currentQName = QName.create(currentQName, BindingReflections.findQName(currentArg.getType()).getLocalName());
            }
            final Optional<DataNodeContainer> potential = findDataNodeContainer(currentContainer.get(), currentQName);
            if (potential.isPresent()) {
                currentContainer = potential;
            } else {
                return Optional.absent();
            }
        }
        return currentContainer;
    }

    private static Optional<DataNodeContainer> findNotification(final SchemaContext ctx, final QName notificationQName) {
        for (final NotificationDefinition notification : ctx.getNotifications()) {
            if (notification.getQName().equals(notificationQName)) {
                return Optional.of(notification);
            }
        }
        return Optional.absent();
    }

    private static Optional<DataNodeContainer> findDataNodeContainer(final DataNodeContainer ctx,
            final QName targetQName) {

        for (final DataSchemaNode child : ctx.getChildNodes()) {
            if (child instanceof ChoiceSchemaNode) {
                final DataNodeContainer potential = findInCases(((ChoiceSchemaNode) child), targetQName);
                if (potential != null) {
                    return Optional.of(potential);
                }
            } else if (child instanceof DataNodeContainer && child.getQName().equals(targetQName)) {
                return Optional.of((DataNodeContainer) child);
            } else if (child instanceof DataNodeContainer //
                    && child.isAddedByUses() //
                    && child.getQName().getLocalName().equals(targetQName.getLocalName())) {
                return Optional.of((DataNodeContainer) child);
            }

        }
        return Optional.absent();
    }

    private static DataNodeContainer findInCases(final ChoiceSchemaNode choiceNode, final QName targetQName) {
        for (final ChoiceCaseNode caze : choiceNode.getCases()) {
            final Optional<DataNodeContainer> potential = findDataNodeContainer(caze, targetQName);
            if (potential.isPresent()) {
                return potential.get();
            }
        }
        return null;
    }

    private static Optional<DataNodeContainer> findFirstDataNodeContainerInRpcOrAction(final SchemaContext ctx,
            final Class<? extends TreeNode> targetType) {
        final YangModuleInfo moduleInfo;
        try {
            moduleInfo = BindingReflections.getModuleInfo(targetType);
        } catch (final Exception e) {
            throw new IllegalArgumentException(
                    String.format("Failed to load module information for class %s", targetType), e);
        }
        Optional<DataNodeContainer> optional = null;
        optional = findFirst(ctx.getOperations(), moduleInfo, targetType);
        if (optional.isPresent()) {
            return optional;
        } else {
            return findFirst(ctx.getActions(), moduleInfo, targetType);
        }
    }

    private static Optional<DataNodeContainer> findFirst(final Set<? extends OperationDefinition> operations,
            final YangModuleInfo moduleInfo, final Class<? extends TreeNode> targetType) {
        for (final OperationDefinition operation : operations) {
            final String operationNamespace = operation.getQName().getNamespace().toString();
            final String operationRevision = operation.getQName().getFormattedRevision();
            if (moduleInfo.getNamespace().equals(operationNamespace)
                    && moduleInfo.getRevision().equals(operationRevision)) {
                final Optional<DataNodeContainer> potential = findInputOutput(operation, targetType.getSimpleName());
                if(potential.isPresent()) {
                    return potential;
                }
            }
        }
        return Optional.absent();
    }

    private static Optional<DataNodeContainer> findInputOutput(final OperationDefinition operation,
            final String targetType) {
        final String operationName =
                JavaIdentifierNormalizer.normalizeSpecificIdentifier(operation.getQName().getLocalName(),
                JavaIdentifier.CLASS);
        final String actionInputName =
                new StringBuilder(operationName).append(BindingMapping.RPC_INPUT_SUFFIX).toString();
        final String actionOutputName =
                new StringBuilder(operationName).append(BindingMapping.RPC_OUTPUT_SUFFIX).toString();
        if (targetType.equals(actionInputName)) {
            return Optional.of(operation.getInput());
        } else if (targetType.equals(actionOutputName)) {
            return Optional.of(operation.getOutput());
        }
       return Optional.absent();
    }

    /**
     * Find choice schema node in parent by binding class.
     *
     * @param parent
     *            - choice parent
     * @param choiceClass
     *            - choice binding class
     * @return choice schema node if exists, absent() otherwise
     */
    public static Optional<ChoiceSchemaNode> findInstantiatedChoice(final DataNodeContainer parent, final Class<?> choiceClass) {
        return findInstantiatedChoice(parent, BindingReflections.findQName(choiceClass));
    }

    /**
     * Find choice schema node in parent node by qname of choice.
     *
     * @param ctxNode
     *            - parent node
     * @param choiceName
     *            - qname of choice
     * @return choice schema node if exists, absent() otherwise
     */
    public static Optional<ChoiceSchemaNode> findInstantiatedChoice(final DataNodeContainer ctxNode, final QName choiceName) {
        final DataSchemaNode potential = ctxNode.getDataChildByName(choiceName);
        if (potential instanceof ChoiceSchemaNode) {
            return Optional.of((ChoiceSchemaNode) potential);
        }

        return Optional.absent();
    }

    /**
     * Find choice case node in choice schema node.
     *
     * @param instantiatedChoice
     *            - choice
     * @param originalDefinition
     *            - choice case
     * @return choice case node if exists, absent() otherwise
     */
    public static Optional<ChoiceCaseNode> findInstantiatedCase(final ChoiceSchemaNode instantiatedChoice, final ChoiceCaseNode originalDefinition) {
        ChoiceCaseNode potential = instantiatedChoice.getCaseNodeByName(originalDefinition.getQName());
        if(originalDefinition.equals(potential)) {
            return Optional.of(potential);
        }
        if (potential != null) {
            final SchemaNode potentialRoot = SchemaNodeUtils.getRootOriginalIfPossible(potential);
            if (originalDefinition.equals(potentialRoot)) {
                return Optional.of(potential);
            }
        }
        // We try to find case by name, then lookup its root definition
        // and compare it with original definition
        // This solves case, if choice was inside grouping
        // which was used in different module and thus namespaces are
        // different, but local names are still same.
        //
        // Still we need to check equality of definition, because local name is not
        // sufficient to uniquelly determine equality of cases
        //
        potential = instantiatedChoice.getCaseNodeByName(originalDefinition.getQName().getLocalName());
        if(potential != null && (originalDefinition.equals(SchemaNodeUtils.getRootOriginalIfPossible(potential)))) {
            return Optional.of(potential);
        }
        return Optional.absent();
    }
}
