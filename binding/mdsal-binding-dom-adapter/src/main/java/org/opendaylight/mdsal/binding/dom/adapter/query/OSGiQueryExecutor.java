/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.query.QueryExecutor;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@Component(immediate = true)
public final class OSGiQueryExecutor implements QueryExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiQueryFactory.class);

    @Reference
    BindingCodecTree codec;

    private DefaultQueryExecutor delegate;

    @Override
    public <R extends @NonNull ChildOf<@NonNull DataRoot>, T extends @NonNull DataObject> @NonNull QueryResult<T>
            executeQuery(@NonNull final QueryExpression<T> query, final R root) {
        return delegate.executeQuery(query, root);
    }

    @Activate
    void activate() {
        delegate = new DefaultQueryExecutor(verifyNotNull(codec));
        LOG.info("Binding Query Executor activated");
    }

    @Deactivate
    void deactivate() {
        delegate = null;
        LOG.info("Binding Query Executor deactivated");
    }
}
