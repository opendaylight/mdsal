/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.opendaylight.mdsal.binding.api.DataObjectModification.WithDataAfter;
import org.opendaylight.yangtools.binding.DataObject;

/**
 * A {@link DataObjectModification} indicating that a {@link DataObject} was written, either initially introduced or
 * overwritten.
 */
public abstract non-sealed class DataObjectWritten<T extends DataObject> extends WithDataAfter<T> {
    @SuppressWarnings("deprecation")
    protected DataObjectWritten() {
        super(ModificationType.WRITE);
    }
}