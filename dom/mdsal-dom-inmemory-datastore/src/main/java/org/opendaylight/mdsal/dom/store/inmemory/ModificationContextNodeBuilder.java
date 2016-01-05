package org.opendaylight.mdsal.dom.store.inmemory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

abstract class ModificationContextNodeBuilder<T extends WriteableModificationNode> {

    private Map<PathArgument, InteriorNodeBuilder> interiorChildren = new HashMap<>();
    private Map<PathArgument, WriteableSubshardBoundaryNode> boundaryChildren = new HashMap<>();

    protected InteriorNodeBuilder getInterior(PathArgument arg) {
        InteriorNodeBuilder potential = interiorChildren.get(arg);
        if (potential == null) {
            potential = new InteriorNodeBuilder(arg);
            interiorChildren.put(arg, potential);
        }
        return potential;
    }

    protected void addBoundary(PathArgument arg, WriteableSubshardBoundaryNode subshardNode) {
        boundaryChildren.put(arg, subshardNode);
    }

    final T build() {
        HashMap<PathArgument, WriteableModificationNode> buildedChildren =
                new HashMap<PathArgument, WriteableModificationNode>(boundaryChildren);
        for (Entry<PathArgument, InteriorNodeBuilder> interiorNode : interiorChildren.entrySet()) {
            WriteableModificationNode builded = interiorNode.getValue().build();
            buildedChildren.put(builded.getIdentifier(), builded);
        }

        return build(buildedChildren);
    }

    abstract T build(Map<PathArgument, WriteableModificationNode> children);

    private static class InteriorNodeBuilder extends ModificationContextNodeBuilder<WritableInteriorNode> {

        private final PathArgument arg;

        InteriorNodeBuilder(PathArgument arg) {
            this.arg = arg;
        }

        @Override
        WritableInteriorNode build(Map<PathArgument, WriteableModificationNode> children) {
            return new WritableInteriorNode(arg, children);
        }
    }

}