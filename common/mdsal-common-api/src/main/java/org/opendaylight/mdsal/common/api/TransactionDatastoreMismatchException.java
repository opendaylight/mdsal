/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception reported when an attempt to use a different {@link LogicalDatastoreType} than the one available to a
 * particular transaction is bound.
 */
@NonNullByDefault
public class TransactionDatastoreMismatchException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final LogicalDatastoreType expected;
    private final LogicalDatastoreType encountered;

    public TransactionDatastoreMismatchException(final LogicalDatastoreType expected,
            final LogicalDatastoreType encountered, final @Nullable Throwable cause) {
        super("Attempted to access " + encountered + " when bound to " + expected, cause);
        this.expected = requireNonNull(expected);
        this.encountered = requireNonNull(encountered);
    }

    public TransactionDatastoreMismatchException(final LogicalDatastoreType expected,
            final LogicalDatastoreType encountered) {
        this(expected, encountered, null);
    }

    public final LogicalDatastoreType expected() {
        return expected;
    }

    public final LogicalDatastoreType encountered() {
        return encountered;
    }
}
