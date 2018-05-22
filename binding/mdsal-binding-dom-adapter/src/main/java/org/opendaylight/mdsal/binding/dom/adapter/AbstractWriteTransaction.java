/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FluentFuture;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Abstract Base Transaction for transactions which are backed by
 * {@link DOMDataTreeWriteTransaction}.
 */
public abstract class AbstractWriteTransaction<T extends DOMDataTreeWriteTransaction> extends
        AbstractForwardedTransaction<T> {

    protected AbstractWriteTransaction(final T delegate, final BindingToNormalizedNodeCodec codec) {
        super(delegate, codec);
    }

    public final <U extends DataObject> void put(final LogicalDatastoreType store,
            final InstanceIdentifier<U> path, final U data, final boolean createParents) {
        Preconditions.checkArgument(!path.isWildcarded(), "Cannot put data into wildcarded path %s", path);

        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalized = getCodec().toNormalizedNode(path, data);
        if (createParents) {
            ensureParentsByMerge(store, normalized.getKey(), path);
        } else {
            ensureListParentIfNeeded(store,path,normalized);
        }

        getDelegate().put(store, normalized.getKey(), normalized.getValue());
    }

    public final <U extends DataObject> void merge(final LogicalDatastoreType store,
            final InstanceIdentifier<U> path, final U data,final boolean createParents) {
        Preconditions.checkArgument(!path.isWildcarded(), "Cannot merge data into wildcarded path %s", path);

        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalized = getCodec().toNormalizedNode(path, data);
        if (createParents) {
            ensureParentsByMerge(store, normalized.getKey(), path);
        } else {
            ensureListParentIfNeeded(store,path,normalized);
        }

        getDelegate().merge(store, normalized.getKey(), normalized.getValue());
    }

    /**
     * Ensures list parent if item is list, otherwise noop.
     *
     * <p>
     * One of properties of binding specification is that it is imposible
     * to represent list as a whole and thus it is impossible to write
     * empty variation of MapNode without creating parent node, with
     * empty list.
     *
     * <p>
     * This actually makes writes such as
     * <pre>
     * put("Nodes", new NodesBuilder().build());
     * put("Nodes/Node[key]", new NodeBuilder().setKey("key").build());
     * </pre>
     * To result in three DOM operations:
     * <pre>
     * put("/nodes",domNodes);
     * merge("/nodes/node",domNodeList);
     * put("/nodes/node/node[key]",domNode);
     * </pre>
     *
     * <p>
     * In order to allow that to be inserted if necessary, if we know
     * item is list item, we will try to merge empty MapNode or OrderedNodeMap
     * to ensure list exists.
     *
     * @param store Data Store type
     * @param path Path to data (Binding Aware)
     * @param normalized Normalized version of data to be written
     */
    private void ensureListParentIfNeeded(final LogicalDatastoreType store, final InstanceIdentifier<?> path,
            final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalized) {
        if (Identifiable.class.isAssignableFrom(path.getTargetType())) {
            final YangInstanceIdentifier parentMapPath = normalized.getKey().getParent();
            Preconditions.checkArgument(parentMapPath != null, "Map path %s does not have a parent", path);

            final NormalizedNode<?, ?> emptyParent = getCodec().getDefaultNodeFor(parentMapPath);
            getDelegate().merge(store, parentMapPath, emptyParent);
        }
    }

    /**
     * Use {@link YangInstanceIdentifier#getParent()} instead.
     */
    @Deprecated
    protected static Optional<YangInstanceIdentifier> getParent(final YangInstanceIdentifier child) {
        return Optional.fromNullable(child.getParent());
    }

    /**
     * Subclasses of this class are required to implement creation of parent nodes based on
     * behaviour of their underlying transaction.
     *
     * @param store an instance of LogicalDatastoreType
     * @param domPath an instance of YangInstanceIdentifier
     * @param path an instance of InstanceIdentifier
     */
    protected final void ensureParentsByMerge(final LogicalDatastoreType store, final YangInstanceIdentifier domPath,
            final InstanceIdentifier<?> path) {
        final YangInstanceIdentifier parentPath = domPath.getParent();
        if (parentPath != null) {
            final NormalizedNode<?, ?> parentNode = getCodec().instanceIdentifierToNode(parentPath);
            getDelegate().merge(store, YangInstanceIdentifier.create(parentNode.getIdentifier()), parentNode);
        }
    }

    protected final void doDelete(final LogicalDatastoreType store,
            final InstanceIdentifier<?> path) {
        Preconditions.checkArgument(!path.isWildcarded(), "Cannot delete wildcarded path %s", path);

        final YangInstanceIdentifier normalized = getCodec().toYangInstanceIdentifierBlocking(path);
        getDelegate().delete(store, normalized);
    }

    protected final @NonNull FluentFuture<? extends @NonNull CommitInfo> doCommit() {
        return getDelegate().commit();
    }

    protected final boolean doCancel() {
        return getDelegate().cancel();
    }

}
