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
    private static final class Multiple extends YangSourceDefinition {
        private final List<? extends SchemaNode> nodes;

        Multiple(final Module module, final Collection<? extends SchemaNode> nodes) {
            super(module);
            this.nodes = ImmutableList.copyOf(nodes);
        }

        @Override
        public List<? extends SchemaNode> getNodes() {
            return nodes;
        }
    }

    private static final class Single extends YangSourceDefinition {
        private final SchemaNode node;

        Single(final Module module, final SchemaNode node) {
            super(module);
            this.node = requireNonNull(node);
        }

        @Override
        public List<SchemaNode> getNodes() {
            return ImmutableList.of(node);
        }
    }

    private final Module module;

    private YangSourceDefinition(final Module module) {
        this.module = requireNonNull(module);
    }

    public static YangSourceDefinition of(final Module module) {
        return new Multiple(module, ImmutableList.of());
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
    public Module getModule() {
        return module;
    }

    /**
     * Return the defining SchemaNodes.
     *
     * @return defining SchemaNodes, empty if {@link #getModule()} is the defining node.
     */
    public abstract List<? extends SchemaNode> getNodes();
}
