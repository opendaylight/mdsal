/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory.impl;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;

/**
 * A {@link DataTree} and its parameters;
 */
@NonNullByDefault
final class InMemoryTree {
    private static final VarHandle VH_NEXTID;
    private static final VarHandle VH_DEBUG;

    static {
        final var lookup = MethodHandles.lookup();
        try {
            VH_NEXTID = lookup.findVarHandle(InMemoryTree.class, "nextId", long.class);
            VH_DEBUG = lookup.findVarHandle(InMemoryTree.class, "debug", boolean.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    final TreeChangePublisher publisher;
    final DataTree dataTree;
    final String name;

    private volatile boolean debug;
    private volatile long nextId;

    InMemoryTree(final String name, final DataTree dataTree, final TreeChangePublisher publisher, final boolean debug) {
        this.name = requireNonNull(name);
        this.dataTree = requireNonNull(dataTree);
        this.publisher = requireNonNull(publisher);
        this.debug = debug;
    }

    String nextId() {
        return name + "-" + (long) VH_NEXTID.getAndAdd(this, 1L);
    }

    boolean debug() {
        return (boolean) VH_DEBUG.getAcquire(this);
    }

    void debug(final boolean newDebug) {
        debug = newDebug;
    }

    void validate(final DataTreeModification modification) throws DataValidationFailedException {
        dataTree.validate(modification);
    }

    void commit(final DataTreeCandidate candidate) {
        dataTree.commit(candidate);
        publisher.publishCandidate(candidate);
    }

    void close() {
        publisher.close();
    }
}
