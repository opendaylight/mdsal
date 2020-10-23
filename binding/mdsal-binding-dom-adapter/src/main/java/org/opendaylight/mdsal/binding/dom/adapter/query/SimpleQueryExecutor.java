/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.api.query.QueryExecutor;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.dom.api.query.DOMQuery;
import org.opendaylight.mdsal.dom.api.query.DOMQueryLike;
import org.opendaylight.mdsal.dom.spi.query.DOMQueryEvaluator;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

@Beta
@NonNullByDefault
public final class SimpleQueryExecutor implements QueryExecutor {
    private final BindingCodecTree codec;
    private final NormalizedNode<?, ?> root;

    public SimpleQueryExecutor(final BindingCodecTree codec, final NormalizedNode<?, ?> root) {
        this.codec = requireNonNull(codec);
        this.root = requireNonNull(root);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SimpleQueryExecutor(final BindingCodecTree codec, final ChildOf<DataRoot> root) {
        this(codec, ((BindingDataObjectCodecTreeNode) codec.getSubtreeCodec(
            InstanceIdentifier.create(root.implementedInterface()))).serialize(root));
    }

    @Override
    public <T extends DataObject> QueryResult<T> executeQuery(final QueryExpression<T> query) {
        checkArgument(query instanceof DOMQueryLike, "Unsupported expression %s", query);
        final DOMQuery domQuery = ((DOMQueryLike) query).asDOMQuery();
        return new DefaultQueryResult<>(codec, DOMQueryEvaluator.evaluate(domQuery, root));
    }
}
