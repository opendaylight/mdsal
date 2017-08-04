/*
 * Copyright (c) 2017 Pantheon Technologies, s.ro. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DOMDataTreeListenerUtil {
    private static final Logger LOG = LoggerFactory.getLogger(DOMDataTreeListenerUtil.class);

    private DOMDataTreeListenerUtil() {
        throw new UnsupportedOperationException();
    }

    static DataTreeCandidate compressCandidates(final Collection<DataTreeCandidate> input) {
        final Iterator<DataTreeCandidate> it = input.iterator();
        checkArgument(!it.hasNext(), "Input must not be empty");

        final DataTreeCandidate first = it.next();
        if (!it.hasNext()) {
            // Short-circuit
            return first;
        }

        final YangInstanceIdentifier rootPath = first.getRootPath();
        final List<DataTreeCandidateNode> roots = new ArrayList<>(input.size());
        roots.add(first.getRootNode());
        it.forEachRemaining(candidate -> {
            final YangInstanceIdentifier root = candidate.getRootPath();
            checkArgument(rootPath.equals(root), "Expecting root path %s, encountered %s", rootPath, root);
            roots.add(candidate.getRootNode());
        });

        return DataTreeCandidates.newDataTreeCandidate(rootPath, compressNodes(roots));
    }

    private static DataTreeCandidateNode compressNodes(final List<DataTreeCandidateNode> input) {
        checkArgument(!input.isEmpty());

        // Single node: already compressed
        final DataTreeCandidateNode first = input.get(0);
        if (input.size() == 1) {
            return first;
        }

        // Fast path: check last node being a terminal node
        final DataTreeCandidateNode last = input.get(input.size() - 1);
        switch (last.getModificationType()) {
            case DELETE:
            case WRITE:
                return new TerminalDataTreeCandidateNode(last.getIdentifier(), last.getModificationType(),
                    first.getDataBefore().orNull(), last.getDataAfter().orNull());
            case APPEARED:
            case DISAPPEARED:
            case SUBTREE_MODIFIED:
            case UNMODIFIED:
                // No luck, we need to iterate
                return slowCompressNodes(first, input);
            default:
                throw new UnsupportedOperationException("Unsupported modification type " + last.getModificationType());
        }
    }

    private static DataTreeCandidateNode slowCompressNodes(final DataTreeCandidateNode first,
            final List<DataTreeCandidateNode> input) {

        final List<DataTreeCandidateNode> significantNodes = new ArrayList<>(input.size());
        ModificationType modType = ModificationType.UNMODIFIED;
        for (DataTreeCandidateNode node : input) {
            final ModificationType nodeMod = node.getModificationType();

            switch (nodeMod) {
                case UNMODIFIED:
                    // No-op
                    continue;
                case DELETE:
                case WRITE:
                    // Terminal node encountered: we can reset the modification type
                    modType = nodeMod;
                    significantNodes.clear();
                    break;
                case SUBTREE_MODIFIED:
                    switch (modType) {
                        case APPEARED:
                        case SUBTREE_MODIFIED:
                        case WRITE:
                            // Modification of previous state, the result does not change
                            break;
                        case UNMODIFIED:
                            modType = ModificationType.SUBTREE_MODIFIED;
                            break;
                        case DELETE:
                        case DISAPPEARED:
                            throw new IllegalArgumentException("Subtree modification event on " + modType + " node");
                        default:
                            throw new IllegalStateException("Unsupported modification type " + modType);
                    }
                    break;
                case APPEARED:
                    switch (modType) {
                        case DELETE:
                            modType = ModificationType.WRITE;
                            break;
                        case DISAPPEARED:
                            modType = ModificationType.SUBTREE_MODIFIED;
                            break;
                        case UNMODIFIED:
                            modType = ModificationType.APPEARED;
                            break;
                        case APPEARED:
                        case SUBTREE_MODIFIED:
                        case WRITE:
                            throw new IllegalArgumentException("Appear event on " + modType + " node");
                        default:
                            throw new IllegalStateException("Unsupported modification type " + modType);
                    }
                    break;
                case DISAPPEARED:
                    switch (modType) {
                        case APPEARED:
                            modType = ModificationType.SUBTREE_MODIFIED;
                            break;
                        case SUBTREE_MODIFIED:
                        case UNMODIFIED:
                            modType = ModificationType.DISAPPEARED;
                            break;
                        case WRITE:
                            modType = ModificationType.DELETE;
                            break;
                        case DELETE:
                        case DISAPPEARED:
                            throw new IllegalArgumentException("Disappear event on " + modType + " node");
                        default:
                            throw new IllegalStateException("Unsupported modification type " + modType);
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported modification type " + nodeMod);
            }

            significantNodes.add(node);
        }

        final PathArgument identifier = first.getIdentifier();
        final NormalizedNode<?, ?> before = first.getDataBefore().orNull();

        switch (modType) {
            case UNMODIFIED:
                return new TerminalDataTreeCandidateNode(identifier, ModificationType.UNMODIFIED, before, before);
            case DELETE:
                checkArgument(before != null);
                return new TerminalDataTreeCandidateNode(first.getIdentifier(), ModificationType.DELETE, before, null);
            case APPEARED:
                checkArgument(before == null);
                return appearedNode(identifier, before, significantNodes);
            case DISAPPEARED:
                checkArgument(before != null);
                return disappearedNode(identifier, before, significantNodes);
            case SUBTREE_MODIFIED:
                checkArgument(before != null);
                return modifiedNode(identifier, before, significantNodes);
            case WRITE:
                return writtenNode(identifier, before, significantNodes);
            default:
                throw new IllegalStateException("Unsupported modification type " + modType);
        }
    }

    private static DataTreeCandidateNode appearedNode(final PathArgument identifier,
            final NormalizedNode<?, ?> before, final List<DataTreeCandidateNode> nodes) {
        // FIXME: implement this
        throw new UnsupportedOperationException("Not implemented");
    }

    private static DataTreeCandidateNode disappearedNode(final PathArgument identifier,
            final NormalizedNode<?, ?> before, final List<DataTreeCandidateNode> nodes) {
        // FIXME: implement this
        throw new UnsupportedOperationException("Not implemented");
    }

    private static DataTreeCandidateNode modifiedNode(final PathArgument identifier, final NormalizedNode<?, ?> before,
            final List<DataTreeCandidateNode> nodes) {
        // FIXME: implement this
        throw new UnsupportedOperationException("Not implemented");
    }

    private static DataTreeCandidateNode writtenNode(final PathArgument identifier, final NormalizedNode<?, ?> before,
            final List<DataTreeCandidateNode> nodes) {
        if (nodes.size() == 1) {
            return new TerminalDataTreeCandidateNode(identifier, ModificationType.WRITE, before,
                nodes.get(0).getDataAfter().get());
        }

        // FIXME: implement this
        throw new UnsupportedOperationException("Not implemented");
    }
}
