/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import java.util.List;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.dom.api.query.DOMQuery;
import org.opendaylight.mdsal.dom.api.query.DOMQueryLike;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

@Beta
@NonNullByDefault
public final class DefaultQuery<T extends DataObject> implements QueryExpression<T>, DOMQueryLike {
    private final BindingCodecTree codec;
    private final DOMQuery domQuery;

    DefaultQuery(final BindingCodecTree codec, final DOMQuery domQuery) {
        this.codec = requireNonNull(codec);
        this.domQuery = requireNonNull(domQuery);
    }

    @Override
    public DOMQuery asDOMQuery() {
        return domQuery;
    }

    public QueryResult<T> toQueryResult(
            final List<? extends Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> domResult) {
        return new DefaultQueryResult<>(codec, domResult);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("dom", domQuery).toString();
    }
}
