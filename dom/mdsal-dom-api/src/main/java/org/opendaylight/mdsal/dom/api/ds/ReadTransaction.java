/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.ds;

import org.opendaylight.yangtools.concepts.Registration;

/**
 * An atomic read transaction. Should always be managed with a try-with-resources block.
 */
public interface ReadTransaction extends ReadOperations, Registration {
    /**
     * Closes this transaction and releases all resources associated with it.
     */
    @Override
    void close();
}
