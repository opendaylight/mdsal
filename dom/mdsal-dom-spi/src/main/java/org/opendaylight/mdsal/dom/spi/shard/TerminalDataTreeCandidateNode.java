/*
 * Copyright (c) 2017 Pantheon Technologies, s.ro. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Optional;
import java.util.Collection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;

final class TerminalDataTreeCandidateNode implements DataTreeCandidateNode {
    private final ModificationType modificationType;
    private final PathArgument identifier;
    private final NormalizedNode<?, ?> before;
    private final NormalizedNode<?, ?> after;

    TerminalDataTreeCandidateNode(final PathArgument identifier, final ModificationType modificationType,
        final NormalizedNode<?, ?> before, final NormalizedNode<?, ?> after) {
        this.identifier = requireNonNull(identifier);
        this.modificationType = requireNonNull(modificationType);
        this.before = before;
        this.after = after;
    }

    @Override
    public PathArgument getIdentifier() {
        return identifier;
    }

    @Override
    public Collection<DataTreeCandidateNode> getChildNodes() {
        // FIXME: requires restructuring of yang-data-impl to expose helpers
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public DataTreeCandidateNode getModifiedChild(final PathArgument identifier) {
        // FIXME: requires restructuring of yang-data-impl to expose helpers
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ModificationType getModificationType() {
        return modificationType;
    }

    @Override
    public Optional<NormalizedNode<?, ?>> getDataAfter() {
        return Optional.fromNullable(after);
    }

    @Override
    public Optional<NormalizedNode<?, ?>> getDataBefore() {
        return Optional.fromNullable(before);
    }
}
