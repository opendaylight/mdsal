/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract DOM Store Transaction.
 * Convenience super implementation of DOM Store transaction which provides common implementation of
 * {@link #toString()} and {@link #getIdentifier()}.
 * It can optionally capture the context where it was allocated.
 *
 * @param <T> identifier type
 */
@Beta
@NonNullByDefault
public abstract class AbstractDOMStoreTransaction<T> implements DOMStoreTransaction {
    private final @Nullable Throwable debugContext;
    private final T identifier;

    protected AbstractDOMStoreTransaction(final T identifier) {
        this(identifier, false);
    }

    @SuppressFBWarnings("NP_STORE_INTO_NONNULL_FIELD")
    protected AbstractDOMStoreTransaction(final T identifier, final boolean debug) {
        this.identifier = requireNonNull(identifier, "Identifier must not be null.");
        this.debugContext = debug ? new Throwable().fillInStackTrace() : null;
    }

    @Override
    public final T getIdentifier() {
        return identifier;
    }

    /**
     * Return the context in which this transaction was allocated.
     *
     * @return The context in which this transaction was allocated, or null
     *         if the context was not recorded.
     */
    public final @Nullable Throwable getDebugContext() {
        return debugContext;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    /**
     * Add class-specific toString attributes.
     *
     * @param toStringHelper
     *            ToStringHelper instance
     * @return ToStringHelper instance which was passed in
     */
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("id", identifier);
    }
}
