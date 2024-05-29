/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import com.google.common.annotations.Beta;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.OperationFailedException;

/**
 * Utility methods addressing the needs of {@link OperationFailedException} subclasses.
 */
// FIXME: this class should not be needed once OperationFailedException provides these methods
@Beta
@NonNullByDefault
public final class OperationFailedExceptionUtil {
    private OperationFailedExceptionUtil() {
        // Hidden on purpose
    }

    public static <X extends OperationFailedException> X ofCaught(final Throwable caught, final Class<X> type,
            final String operationName, final BiFunction<String, Throwable, X> toException) {
        // If the cause is a TransactionCommitFailedException, return that
        if (type.isInstance(caught)) {
            return type.cast(caught);
        }

        // Unmap ExecutionException's cause if possible
        if (caught instanceof ExecutionException e) {
            final var cause = e.getCause();
            if (type.isInstance(cause)) {
                return type.cast(cause);
            } else if (cause != null) {
                return toException.apply(operationName + " execution failed", cause);
            }
        }

        // Otherwise return an instance of the specified type with the original cause.
        final String message;
        if (caught instanceof InterruptedException) {
            message = operationName + " was interupted";
        } else if (caught instanceof CancellationException) {
            message = operationName + " was cancelled";
        } else {
            // We really should not get here but need to cover it anyway for completeness.
            message = operationName + " encountered unexpected failure";
        }
        return toException.apply(message, caught);
    }
}
