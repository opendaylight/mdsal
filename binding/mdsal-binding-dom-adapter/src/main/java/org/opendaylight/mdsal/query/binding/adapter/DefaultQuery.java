/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.dom.api.query.DOMQuery;
import org.opendaylight.mdsal.dom.api.query.DOMQueryLike;
import org.opendaylight.yangtools.yang.binding.DataObject;

final class DefaultQuery<T extends DataObject> implements QueryExpression<T>, DOMQueryLike {
    private final @NonNull DOMQuery domQuery;

    DefaultQuery(final DOMQuery domQuery) {
        this.domQuery = requireNonNull(domQuery);
    }

    @Override
    public DOMQuery asDOMQuery() {
        return domQuery;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("dom", domQuery).toString();
    }
}
