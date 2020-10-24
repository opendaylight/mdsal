/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.query.MatchBuilderPath.LeafReference;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryStructureException;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingSchemaMapping;
import org.opendaylight.mdsal.dom.api.query.DOMQuery;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate;
import org.opendaylight.mdsal.query.binding.adapter.LambdaDecoder.LambdaTarget;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;

final class QueryBuilderState {
    static final class BoundMethod implements Immutable {
        final @NonNull YangInstanceIdentifier parentPath;
        final @NonNull BindingCodecTreeNode methodCodec;

        BoundMethod(final YangInstanceIdentifier parentPath, final BindingCodecTreeNode methodCodec) {
            this.parentPath = requireNonNull(parentPath);
            this.methodCodec = requireNonNull(methodCodec);
        }
    }

    private final BindingCodecTree codec;

    private final List<DOMQueryPredicate> predicates = new ArrayList<>();
    private final YangInstanceIdentifier root;
    private YangInstanceIdentifier absoluteSelect;
    private YangInstanceIdentifier relativeSelect;

    QueryBuilderState(final BindingCodecTree codec, final InstanceIdentifier<?> root) {
        this.codec = requireNonNull(codec);
        this.root = fromBinding(root);
    }

    void setSelectPath(final @NonNull InstanceIdentifier<?> selectPath) {
        checkState(root != null, "Root path has not been set yet");
        checkState(relativeSelect == null, "Select path has already been set to %s", relativeSelect);

        absoluteSelect = fromBinding(selectPath);
        relativeSelect = absoluteSelect.relativeTo(root)
                .orElseThrow(() -> new IllegalStateException(root + " is not an ancestor of " + absoluteSelect));
    }

    @NonNull BoundMethod bindMethod(final @NonNull InstanceIdentifier<?> bindingPath,
            final @NonNull LeafReference<?, ?> ref) {
        // Verify bindingPath, which will give us something to fish in
        final BindingDataObjectCodecTreeNode<?> targetCodec = codec.getSubtreeCodec(bindingPath);
        checkState(targetCodec != null, "Failed to find codec for %s", bindingPath);

        final WithStatus targetSchema = targetCodec.getSchema();
        verify(targetSchema instanceof DataNodeContainer, "Unexpected target schema %s", targetSchema);

        final LambdaTarget targetLeaf = LambdaDecoder.resolveLambda(ref);
        verify(targetLeaf.targetClass.equals(bindingPath.getTargetType().getName()), "Mismatched target %s and path %s",
            targetLeaf, bindingPath);
        final DataSchemaNode child = findChild((DataNodeContainer) targetSchema, targetLeaf.targetMethod);
        final YangInstanceIdentifier absTarget = fromBinding(bindingPath);
        final YangInstanceIdentifier relTarget = absTarget.relativeTo(absoluteSelect)
                .orElseThrow(() -> new IllegalStateException(absoluteSelect + " is not an ancestor of " + absTarget));

        return new BoundMethod(relTarget, targetCodec.yangPathArgumentChild(new NodeIdentifier(child.getQName())));
    }

    void addPredicate(final DOMQueryPredicate predicate) {
        predicates.add(requireNonNull(predicate));
    }

    <T extends DataObject> @NonNull QueryExpression<T> buildQuery() {
        return new DefaultQuery<>(new DOMQuery(root, relativeSelect, predicates));
    }

    private @NonNull YangInstanceIdentifier fromBinding(final InstanceIdentifier<?> bindingId) {
        return codec.getInstanceIdentifierCodec().fromBinding(bindingId);
    }

    private static DataSchemaNode findChild(final DataNodeContainer parent, final String methodName) {
        for (DataSchemaNode child : parent.getChildNodes()) {
            if (methodName.equals(BindingSchemaMapping.getGetterMethodName(child))) {
                return child;
            }
        }
        throw new QueryStructureException("Failed to find schema matching " + methodName + " in " + parent);
    }
}
