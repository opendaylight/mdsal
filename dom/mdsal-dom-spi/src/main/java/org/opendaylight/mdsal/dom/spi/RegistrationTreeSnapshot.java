/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.locks.Lock;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.AbstractRegistration;

/**
 * A stable read-only snapshot of a {@link AbstractRegistrationTree}.
 *
 * @author Robert Varga
 */
@NonNullByDefault
public final class RegistrationTreeSnapshot<T> extends AbstractRegistration {
    private final RegistrationTreeNode<T> node;
    private final Lock lock;

    RegistrationTreeSnapshot(final Lock lock, final RegistrationTreeNode<T> node) {
        this.lock = requireNonNull(lock);
        this.node = requireNonNull(node);
    }

    public RegistrationTreeNode<T> getRootNode() {
        return node;
    }

    @Override
    protected void removeRegistration() {
        lock.unlock();
    }
}
