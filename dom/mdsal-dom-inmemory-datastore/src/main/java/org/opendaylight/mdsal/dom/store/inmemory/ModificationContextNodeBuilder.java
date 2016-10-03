/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

public abstract class ModificationContextNodeBuilder {

    private final Map<PathArgument, InteriorNodeBuilder> interiorChildren = new LinkedHashMap<>();
    private final Map<PathArgument, WriteableSubshardBoundaryNode> boundaryChildren = new HashMap<>();

    private InteriorNodeBuilder lastPotential;

    protected ModificationContextNodeBuilder getInterior(final PathArgument arg) {
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

    protected final Map<PathArgument, WriteableModificationNode> buildChildren() {
        final Map<PathArgument, WriteableModificationNode> builtChildren = new HashMap<>(boundaryChildren);
        for (InteriorNodeBuilder interiorNode : interiorChildren.values()) {
            WriteableModificationNode builded = interiorNode.build();
            builtChildren.put(builded.getIdentifier(), builded);
        }

        return builtChildren;
    }

    private static final class InteriorNodeBuilder extends ModificationContextNodeBuilder {
        private final PathArgument arg;

        InteriorNodeBuilder(final PathArgument arg) {
            this.arg = Preconditions.checkNotNull(arg);
        }

        WritableInteriorNode build() {
            return new WritableInteriorNode(arg, buildChildren());
        }
    }
}