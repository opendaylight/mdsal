/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;

/**
 * Exception thrown when a loop is detected in the way {@link DataTreeListener} and
 * {@link DataTreeProducer} instances would be connected.
 */
@Beta
public class DataTreeLoopException extends DataTreeProducerException {

    private static final long serialVersionUID = 1L;

    public DataTreeLoopException(@Nonnull final String message, @Nonnull final Throwable cause) {
        super(message, cause);
    }

    public DataTreeLoopException(@Nonnull final String message) {
        super(message);
    }
}
