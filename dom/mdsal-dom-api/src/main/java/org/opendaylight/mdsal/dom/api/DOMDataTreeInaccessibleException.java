/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Failure reported when a data tree is no longer accessible.
 *
 * @deprecated This interface is scheduled for removal in the next major release.
 */
@Deprecated(forRemoval = true)
@NonNullByDefault
public class DOMDataTreeInaccessibleException extends DOMDataTreeListeningException {
    private static final long serialVersionUID = 1L;
    private final DOMDataTreeIdentifier treeIdentifier;

    public DOMDataTreeInaccessibleException(final DOMDataTreeIdentifier treeIdentifier, final String message) {
        super(message);
        this.treeIdentifier = requireNonNull(treeIdentifier);
    }

    public DOMDataTreeInaccessibleException(final DOMDataTreeIdentifier treeIdentifier,
            final String message, final Throwable cause) {
        super(message, requireNonNull(cause));
        this.treeIdentifier = requireNonNull(treeIdentifier);
    }

    public final DOMDataTreeIdentifier getTreeIdentifier() {
        return treeIdentifier;
    }
}
