/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingSchemaMapping;
import org.opendaylight.mdsal.query.binding.adapter.LambdaDecoder.LambdaTarget;
import org.opendaylight.mdsal.query.binding.api.MatchBuilderPath.LeafReference;
import org.opendaylight.mdsal.query.binding.api.Query;
import org.opendaylight.mdsal.query.binding.api.QueryStructureException;
import org.opendaylight.mdsal.query.dom.api.DOMQuery;
import org.opendaylight.mdsal.query.dom.api.DOMQueryBuilder;
import org.opendaylight.mdsal.query.dom.api.DOMQueryPredicate;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;

final class AdaptingQueryBuilder {
    static final class BoundMethod implements Immutable {
        final @NonNull BindingDataObjectCodecTreeNode<?> parentCodec;
        final @NonNull YangInstanceIdentifier parentPath;
        final @NonNull DataSchemaNode methodSchema;

        BoundMethod(final BindingDataObjectCodecTreeNode<?> parentCodec, final YangInstanceIdentifier parentPath,
                final DataSchemaNode methodSchema) {
            this.parentCodec = requireNonNull(parentCodec);
            this.parentPath = requireNonNull(parentPath);
            this.methodSchema = requireNonNull(methodSchema);
        }
    }

    private final DOMQueryBuilder builder = new DOMQueryBuilder();
    private final BindingCodecTree codec;

    AdaptingQueryBuilder(final BindingCodecTree codec) {
        this.codec = requireNonNull(codec);
    }

    void setRootPath(final @NonNull InstanceIdentifier<?> rootPath) {
        builder.setRootPath(fromBinding(rootPath));
    }

    void setSelectPath(final @NonNull InstanceIdentifier<?> selectPath) {
        builder.setSelectPath(fromBinding(selectPath));
    }

    @NonNull BoundMethod bindMethod(final @NonNull InstanceIdentifier<?> bindingPath,
            final @NonNull LeafReference<?, ?> ref) {
        // Verify bindingPath, which will give us something to fish in
        final BindingDataObjectCodecTreeNode<?> targetCodec = codec.getSubtreeCodec(bindingPath);
        final WithStatus targetSchema = targetCodec.getSchema();
        verify(targetSchema instanceof DataNodeContainer, "Unexpected target schema %s", targetSchema);

        final LambdaTarget targetLeaf = LambdaDecoder.resolveLambda(ref);
        verify(targetLeaf.targetClass.equals(bindingPath.getTargetType().getName()), "Mismatched target %s and path %s",
            targetLeaf, bindingPath);
        final DataSchemaNode child = findChild((DataNodeContainer) targetSchema, targetLeaf.targetMethod);

        return new BoundMethod(targetCodec, fromBinding(bindingPath), child);
    }

    void addPredicate(final BoundMethod method, final DOMQueryPredicate predicate) {
        builder.addPredicate(method.parentPath.node(method.methodSchema.getQName()), predicate);
    }

    <T extends DataObject> @NonNull Query<T> buildQuery() {
        final DOMQuery domQuery = builder.build();
        return new DefaultQuery<>(domQuery);
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
