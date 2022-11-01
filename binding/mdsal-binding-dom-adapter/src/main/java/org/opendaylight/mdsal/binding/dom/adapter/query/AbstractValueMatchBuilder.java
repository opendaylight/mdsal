/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.query.ValueMatch;
import org.opendaylight.mdsal.binding.dom.adapter.query.QueryBuilderState.BoundMethod;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate;
import org.opendaylight.mdsal.dom.api.query.DOMQueryPredicate.Match;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

abstract class AbstractValueMatchBuilder<T extends DataObject, V> {
    private final QueryBuilderState builder;
    private final InstanceIdentifier<T> select;
    private final BoundMethod method;

    AbstractValueMatchBuilder(final QueryBuilderState builder, final InstanceIdentifier<T> select,
            final BoundMethod method) {
        this.builder = requireNonNull(builder);
        this.select = requireNonNull(select);
        this.method = requireNonNull(method);
    }

    final @NonNull ValueMatch<T> nonNullImpl() {
        return withMatch(Match.exists());
    }

    final @NonNull ValueMatch<T> isNullImpl() {
        return withMatch(Match.exists().negate());
    }

    final @NonNull ValueMatch<T> valueEqualsImpl(final V value) {
        return withMatch(Match.valueEquals(value));
    }

    final YangInstanceIdentifier relativePath() {
        return method.parentPath.node(((DataSchemaNode) method.methodCodec.getSchema()).getQName());
    }

    final @NonNull ValueMatch<T> withMatch(final Match match) {
        // FIXME: this does not quite take value codec into account :(
        builder.addPredicate(DOMQueryPredicate.of(relativePath(), match));
        return new DefaultValueMatch<>(builder, select);
    }
}
