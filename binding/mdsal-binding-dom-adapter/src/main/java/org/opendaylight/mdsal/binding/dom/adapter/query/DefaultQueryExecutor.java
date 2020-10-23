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
import java.util.ServiceLoader;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.api.query.QueryExecutor;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.dom.api.query.DOMQuery;
import org.opendaylight.mdsal.dom.api.query.DOMQueryLike;
import org.opendaylight.mdsal.dom.spi.query.DOMQueryEvaluator;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

@Beta
@MetaInfServices
@NonNullByDefault
@Singleton
public final class DefaultQueryExecutor implements QueryExecutor {
    private static final class DefaultItem<T extends DataObject> implements Immutable {
        private final InstanceIdentifier<T> path;
        private final T object;

        DefaultItem(final InstanceIdentifier<T> path, final T object) {
            this.path = requireNonNull(path);
            this.object = requireNonNull(object);
        }

        public T object() {
            return object;
        }

        public InstanceIdentifier<T> path() {
            return path;
        }

        @Override
        public int hashCode() {
            return path.hashCode() * 31 + object.hashCode();
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            if (!(obj instanceof DefaultItem)) {
                return false;
            }
            final DefaultItem<?> other = (DefaultItem<?>) obj;
            return path.equals(other.path) && obj.equals(other.object);
        }
    }


    private final BindingCodecTree codec;

    public DefaultQueryExecutor() {
        this(ServiceLoader.load(BindingCodecTree.class).findFirst().orElseThrow());
    }

    @Inject
    public DefaultQueryExecutor(final BindingCodecTree codec) {
        this.codec = requireNonNull(codec);
    }

    @Override
    public <R extends ChildOf<DataRoot>, T extends DataObject> QueryResult<T> executeQuery(
            final QueryExpression<T> query, final R root) {
        checkArgument(query instanceof DOMQueryLike, "Unsupported expression %s", query);
        final DOMQuery domQuery = ((DOMQueryLike) query).asDOMQuery();

        @SuppressWarnings({ "rawtypes", "unchecked" })
        final NormalizedNode<?, ?> domData = ((BindingDataObjectCodecTreeNode) codec.getSubtreeCodec(
            InstanceIdentifier.create(root.implementedInterface()))).serialize(root);

        return new DefaultQueryResult<>(codec, DOMQueryEvaluator.evaluate(domQuery, domData));
    }
}
