/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.ds;

/**
 * A transaction that provides read/write access to a logical data store. For more information on usage and examples,
 * please see the documentation in {@link WriteTransaction} and {@link ReadOperations}.
 */
public interface ReadWriteTransaction extends WriteTransaction, ReadOperations {
    // Nothing else
}
