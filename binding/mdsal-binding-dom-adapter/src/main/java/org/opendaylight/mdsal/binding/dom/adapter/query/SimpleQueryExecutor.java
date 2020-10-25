/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.query.QueryExecutor;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.dom.spi.query.DOMQueryEvaluator;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Beta
public final class SimpleQueryExecutor implements QueryExecutor {
    private final BindingCodecTree codec;
    private final NormalizedNode<?, ?> root;

    public SimpleQueryExecutor(final BindingCodecTree codec, final NormalizedNode<?, ?> root) {
        this.codec = requireNonNull(codec);
        this.root = requireNonNull(root);
    }

    @Override
    public <T extends @NonNull DataObject> QueryResult<T> executeQuery(final QueryExpression<T> query) {
        checkArgument(query instanceof DefaultQuery, "Unsupported expression %s", query);
        final DefaultQuery<T> defaultQuery = (DefaultQuery<T>) query;
        return defaultQuery.toQueryResult(DOMQueryEvaluator.evaluateOnRoot(defaultQuery.asDOMQuery(), root));
    }

    public static @NonNull Builder builder(final BindingCodecTree codec) {
        return new Builder(codec);
    }

    public static final class Builder implements org.opendaylight.yangtools.concepts.Builder<SimpleQueryExecutor> {
        private final DataContainerNodeBuilder<NodeIdentifier, ContainerNode> rootBuilder = Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(SchemaContext.NAME));
        private final BindingCodecTree codec;

        Builder(final BindingCodecTree codec) {
            this.codec = requireNonNull(codec);
        }

        public <T extends ChildOf<? extends DataRoot>> @NonNull Builder add(final @NonNull T data) {
            @SuppressWarnings("unchecked")
            final BindingDataObjectCodecTreeNode<T> dataCodec = (BindingDataObjectCodecTreeNode<T>)
                codec.getSubtreeCodec(InstanceIdentifier.create(data.implementedInterface()));
            rootBuilder.withChild((DataContainerChild<?, ?>) verifyNotNull(dataCodec).serialize(data));
            return this;
        }

        @Override
        public SimpleQueryExecutor build() {
            return new SimpleQueryExecutor(codec, rootBuilder.build());
        }
    }
}
