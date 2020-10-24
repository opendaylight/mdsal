/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.query.ValueMatch;
import org.opendaylight.mdsal.binding.api.query.ValueMatchBuilder;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate.Exists;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate.NotExists;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate.ValueEquals;
import org.opendaylight.mdsal.query.binding.adapter.QueryBuilderState.BoundMethod;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

abstract class AbstractValueMatchBuilder<T extends DataObject, V> implements ValueMatchBuilder<T, V> {
    private final QueryBuilderState builder;
    private final InstanceIdentifier<T> select;
    private final BoundMethod method;

    AbstractValueMatchBuilder(final QueryBuilderState builder, final InstanceIdentifier<T> select,
            final BoundMethod method) {
        this.builder = requireNonNull(builder);
        this.select = requireNonNull(select);
        this.method = requireNonNull(method);
    }

    @Override
    public final ValueMatch<T> nonNull() {
        return withPredicate(new Exists(relativePath()));
    }

    @Override
    public final ValueMatch<T> isNull() {
        return withPredicate(new NotExists(relativePath()));
    }

    @Override
    public final ValueMatch<T> valueEquals(final V value) {
        return withPredicate(new ValueEquals<>(relativePath(), value));
    }

    final YangInstanceIdentifier relativePath() {
        return method.parentPath.node(((DataSchemaNode) method.methodCodec.getSchema()).getQName());
    }

    final @NonNull ValueMatch<T> withPredicate(final DOMQueryPredicate predicate) {
        // FIXME: this does not quite take value codec into account :(
        builder.addPredicate(predicate);
        return new DefaultValueMatch<>(builder, select);
    }
}
