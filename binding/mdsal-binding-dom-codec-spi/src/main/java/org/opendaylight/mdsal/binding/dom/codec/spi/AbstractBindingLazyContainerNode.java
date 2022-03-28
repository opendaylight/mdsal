/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingObject;
import java.util.Collection;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingLazyContainerNode;
import org.opendaylight.yangtools.concepts.PrettyTree;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

/**
 * A {@link ContainerNode} backed by a binding {@link DataObject}, with lazy instantiation of the ContainerNode view.
 * This class is thread-safe.
 *
 * @param <T> Binding DataObject type
 * @param <C> Context type
 * @author Robert Varga
 */
@Beta
public abstract class AbstractBindingLazyContainerNode<T extends DataObject, C> extends ForwardingObject
        implements BindingLazyContainerNode<T> {
    private final @NonNull NodeIdentifier identifier;
    private final @NonNull T bindingData;

    private volatile @Nullable ContainerNode delegate;
    @GuardedBy("this")
    private @Nullable C context;

    protected AbstractBindingLazyContainerNode(final @NonNull NodeIdentifier identifier, final @NonNull T bindingData,
            final C context) {
        this.identifier = requireNonNull(identifier);
        this.bindingData = requireNonNull(bindingData);
        this.context = context;
    }

    @Override
    public final @NonNull T getDataObject() {
        return bindingData;
    }

    @Override
    public @NonNull NodeIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public final ContainerNode getDelegate() {
        return delegate();
    }

    @Override
    public Collection<DataContainerChild> body() {
        return delegate().body();
    }

    @Override
    public DataContainerChild childByArg(final PathArgument child) {
        return delegate().childByArg(child);
    }

    @Override
    public PrettyTree prettyTree() {
        // Do not touch delegate() until we really need to
        return new PrettyTree() {
            @Override
            public void appendTo(final StringBuilder sb, final int depth) {
                delegate().prettyTree().appendTo(sb, depth);
            }
        };
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ContainerNode)) {
            return false;
        }
        final ContainerNode other = (ContainerNode) obj;
        return delegate().equals(other);
    }

    @Override
    protected final @NonNull ContainerNode delegate() {
        ContainerNode local = delegate;
        if (local == null) {
            synchronized (this) {
                local = delegate;
                if (local == null) {
                    local = delegate = requireNonNull(computeContainerNode(context));
                    context = null;
                }
            }
        }

        return local;
    }

    @GuardedBy("this")
    protected abstract @NonNull ContainerNode computeContainerNode(C context);
}
