/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import org.opendaylight.yangtools.concepts.Registration;

/**
 * A transaction that provides read access to a logical data store.
 *
 * <p>
 * View of the data tree is a stable point-in-time snapshot of the current data tree state when the
 * transaction was created. It's state and underlying data tree is not affected by other
 * concurrently running transactions.
 *
 * <p>
 * <b>Implementation Note:</b> This interface is not intended to be implemented by users of MD-SAL,
 * but only to be consumed by them.
 *
 * <h2>Transaction isolation example</h2>
 * Lets assume initial state of data tree for <code>PATH</code> is <code>A</code>.
 *
 * <code>
 * txRead = broker.newReadOnlyTransaction(); // read Transaction is snapshot of data
 * txWrite = broker.newReadWriteTransactoin(); // concurrent write transaction
 * txRead.read(OPERATIONAL, PATH).get(); // will return Optional containing A
 * txWrite = broker.put(OPERATIONAL, PATH, B); // writes B to PATH
 * txRead.read(OPERATIONAL, PATH).get(); // still returns Optional containing A
 * txWrite.submit().get(); // data tree is updated, PATH contains B
 * txRead.read(OPERATIONAL, PATH).get(); // still returns Optional containing A
 * txAfterCommit = broker.newReadOnlyTransaction(); // read Transaction is snapshot of new state
 * txAfterCommit.read(OPERATIONAL, PATH).get(); // returns Optional containing B;
 * </code>
 *
 * <p>
 * <b>Note:</b> example contains blocking calls on future only to illustrate that action happened after other
 * asynchronous action. Use of blocking call {@link com.google.common.util.concurrent.FluentFuture#get()} is
 * discouraged for most uses and you should use
 * {@link com.google.common.util.concurrent.FluentFuture#addCallback(com.google.common.util.concurrent.FutureCallback,
 * java.util.concurrent.Executor)} or other functions from {@link com.google.common.util.concurrent.Futures} to register
 * more specific listeners.
 */
public interface DOMDataTreeReadTransaction extends DOMDataTreeTransaction, DOMDataTreeReadOperations, Registration {
    /**
     * Closes this transaction and releases all resources associated with it.
     */
    @Override
    void close();
}
