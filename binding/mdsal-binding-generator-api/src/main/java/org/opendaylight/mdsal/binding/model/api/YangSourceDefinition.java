/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

/**
 * DTO capturing the YANG source definition which lead to a {@link GeneratedType} being emitted.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public abstract class YangSourceDefinition {
    public static final class Multiple extends YangSourceDefinition {
        private final List<? extends SchemaNode> nodes;

        Multiple(final Module module, final Collection<? extends SchemaNode> nodes) {
            super(module);
            this.nodes = ImmutableList.copyOf(nodes);
        }

        /**
         * Return the defining SchemaNodes.
         *
         * @return defining SchemaNodes, guaranteed to be non-empty
         */
        public List<? extends SchemaNode> getNodes() {
            return nodes;
        }
    }

    public static final class Single extends YangSourceDefinition {
        private final DocumentedNode node;

        Single(final Module module, final DocumentedNode node) {
            super(module);
            this.node = requireNonNull(node);
        }

        public DocumentedNode getNode() {
            return node;
        }
    }

    private final Module module;

    private YangSourceDefinition(final Module module) {
        this.module = requireNonNull(module);
    }

    public static YangSourceDefinition of(final Module module) {
        return new Single(module, module);
    }

    public static YangSourceDefinition of(final Module module, final SchemaNode node) {
        return new Single(module, node);
    }

    public static YangSourceDefinition of(final Module module, final Collection<? extends SchemaNode> nodes) {
        checkArgument(!nodes.isEmpty());
        return new Multiple(module, nodes);
    }

    /**
     * Return the defining YANG module.
     *
     * @return Defining YANG module.
     */
    public final Module getModule() {
        return module;
    }
}
