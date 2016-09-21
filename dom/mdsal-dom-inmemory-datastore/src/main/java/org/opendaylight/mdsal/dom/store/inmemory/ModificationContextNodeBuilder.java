/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

abstract class ModificationContextNodeBuilder<T extends WriteableModificationNode> {

    private final Map<PathArgument, InteriorNodeBuilder> interiorChildren = new HashMap<>();
    private final Map<PathArgument, WriteableSubshardBoundaryNode> boundaryChildren = new HashMap<>();

    protected InteriorNodeBuilder getInterior(final PathArgument arg) {
        InteriorNodeBuilder potential = interiorChildren.get(arg);
        if (potential == null) {
            potential = new InteriorNodeBuilder(arg);
            interiorChildren.put(arg, potential);
        }
        return potential;
    }

    protected void addBoundary(final PathArgument arg, final WriteableSubshardBoundaryNode subshardNode) {
        boundaryChildren.put(arg, subshardNode);
    }

    final T build() {
        final Map<PathArgument, WriteableModificationNode> builtChildren = new HashMap<>(boundaryChildren);
        for (InteriorNodeBuilder interiorNode : interiorChildren.values()) {
            WriteableModificationNode builded = interiorNode.build();
            builtChildren.put(builded.getIdentifier(), builded);
        }

        return build(builtChildren);
    }

    abstract T build(Map<PathArgument, WriteableModificationNode> children);

    private static class InteriorNodeBuilder extends ModificationContextNodeBuilder<WritableInteriorNode> {

        private final PathArgument arg;

        InteriorNodeBuilder(final PathArgument arg) {
            this.arg = arg;
        }

        @Override
        WritableInteriorNode build(final Map<PathArgument, WriteableModificationNode> children) {
            return new WritableInteriorNode(arg, children);
        }
    }

}