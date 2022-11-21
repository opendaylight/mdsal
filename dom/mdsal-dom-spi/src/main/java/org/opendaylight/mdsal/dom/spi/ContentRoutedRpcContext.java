/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2022 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.odlext.model.api.ContextReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

/**
 * Information pertaining to the concept of {@code Routed RPCs}. "Routed" in this context means being bound to a certain
 * data tree node, identified by a {@code leaf} within the RPC input. This is very much similar to a RFC7950
 * {@code action}, except it operates strictly within the confines of RFC6020.
 *
 * @param identity QName which refers RPC Routing context {@code identity}
 * @param leaf QName of the leaf in which RPC Route is stored
 */
public record ContentRoutedRpcContext(@NonNull QName identity, @NonNull QName leaf) {
    public ContentRoutedRpcContext {
        requireNonNull(identity);
        requireNonNull(identity);
    }

    /**
     * Attempt to construct a {@link ContentRoutedRpcContext} for a particular {@code rpc}.
     *
     * @param rpc RPC statement
     * @return A {@link ContentRoutedRpcContext}, or {@code null} if the RPC does not contain context information
     */
    public static @Nullable ContentRoutedRpcContext forRpc(final RpcEffectiveStatement rpc) {
        final var input = rpc.findFirstEffectiveSubstatement(InputEffectiveStatement.class)
            .orElseThrow(() -> new IllegalArgumentException("Cannot find input in " + rpc));

        for (var stmt : input.effectiveSubstatements()) {
            // TODO: LeafEffectiveStatement instead? Because that is what we are promising for #leaf()'s QName
            if (stmt instanceof SchemaTreeEffectiveStatement<?> schemaStmt) {
                final var context =
                    stmt.findFirstEffectiveSubstatementArgument(ContextReferenceEffectiveStatement.class);
                if (context.isPresent()) {
                    return new ContentRoutedRpcContext(context.orElseThrow(), schemaStmt.argument());
                }
            }
        }

        return null;
    }
}
