/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.odlext.model.api.ContextReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

public abstract sealed class RpcRoutingStrategy implements Identifiable<QName> {
    private final @NonNull QName identifier;

    private RpcRoutingStrategy(final QName identifier) {
        this.identifier = requireNonNull(identifier);
    }

    /**
     * Returns leaf QName in which RPC Route is stored.
     * @return leaf QName in which RPC Route is stored
     * @throws UnsupportedOperationException If RPC is not content routed.
     *      ({@link #isContextBasedRouted()} returned <code>false</code>)
     */
    public abstract QName getLeaf();

    /**
     * Returns identity QName which represents RPC Routing context.
     * @return identity QName which represents RPC Routing context
     * @throws UnsupportedOperationException If RPC is not content routed.
     *      ({@link #isContextBasedRouted()} returned <code>false</code>)
     */
    public abstract QName getContext();

    @Override
    public final QName getIdentifier() {
        return identifier;
    }

    /**
     * Returns true if RPC is routed by context.
     *
     * @return true if RPC is routed by content.
     */
    public abstract boolean isContextBasedRouted();

    public static RpcRoutingStrategy from(final RpcDefinition rpc) {
        // FIXME: deprecate context-reference
        for (EffectiveStatement<?, ?> stmt : rpc.getInput().asEffectiveStatement().effectiveSubstatements()) {
            if (stmt instanceof SchemaTreeEffectiveStatement<?> schemaStmt) {
                final var context =
                    stmt.findFirstEffectiveSubstatementArgument(ContextReferenceEffectiveStatement.class);
                if (context.isPresent()) {
                    return new RoutedRpcStrategy(rpc.getQName(), context.orElseThrow(), schemaStmt.argument());
                }
            }
        }
        return new GlobalRpcStrategy(rpc.getQName());
    }

    private static final class RoutedRpcStrategy extends RpcRoutingStrategy {
        private final QName context;
        private final QName leaf;

        private RoutedRpcStrategy(final QName identifier, final QName ctx, final QName leaf) {
            super(identifier);
            context = requireNonNull(ctx);
            this.leaf = requireNonNull(leaf);
        }

        @Override
        public QName getContext() {
            return context;
        }

        @Override
        public QName getLeaf() {
            return leaf;
        }

        @Override
        public boolean isContextBasedRouted() {
            return true;
        }
    }

    private static final class GlobalRpcStrategy extends RpcRoutingStrategy {
        GlobalRpcStrategy(final QName identifier) {
            super(identifier);
        }

        @Override
        public boolean isContextBasedRouted() {
            return false;
        }

        @Override
        public QName getContext() {
            throw new UnsupportedOperationException("Non-routed strategy does not have a context");
        }

        @Override
        public QName getLeaf() {
            throw new UnsupportedOperationException("Non-routed strategy does not have a context");
        }
    }
}
