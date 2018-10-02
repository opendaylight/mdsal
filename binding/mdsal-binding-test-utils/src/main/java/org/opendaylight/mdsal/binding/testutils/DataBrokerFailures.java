/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testutils;

import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Configures a DataBroker to simulate failures, useful for tests.
 *
 * @author Michael Vorburger.ch
 */
public interface DataBrokerFailures {

    /**
     * Fails all future reads.
     *
     * @param exception a {@link ReadFailedException} to throw from a
     * {@link ReadTransaction#read(LogicalDatastoreType, InstanceIdentifier)} call.
     */
    void failReads(ReadFailedException exception);

    /**
     * Fails N future reads.
     *
     * @param howManyTimes how many times to throw the passed exception, until it resets.
     *
     * @param exception a {@link ReadFailedException} to throw from a
     * {@link ReadTransaction#read(LogicalDatastoreType, InstanceIdentifier)} call.
     */
    void failReads(int howManyTimes, ReadFailedException exception);

    /**
     * Fails all future Transaction commits.
     *
     * @param exception an Exception to throw from a {@link WriteTransaction#commit()} method.
     */
    void failCommits(TransactionCommitFailedException exception);

    /**
     * Fails N future Transaction commits.
     *
     * @param howManyTimes
     *               how many times to throw the passed exception, until it resets
     *
     * @param exception an Exception to throw from a {@link WriteTransaction#commit()} method.
     */
    void failCommits(int howManyTimes, TransactionCommitFailedException exception);

    /**
     * To simulate scenarios where even though the transaction throws a
     * TransactionCommitFailedException (caused by
     * akka.pattern.AskTimeoutException) it eventually succeeds. These timeouts
     * are typically seen in scaled cluster environments under load. The new
     * tell-based protocol, which will soon be enabled by default (c/61002),
     * adds internal retries for transactions, making the application not to
     * handle such scenarios.
     */
    void failButCommitAnyway();

    /**
     * Resets any earlier {@link #failReads(ReadFailedException)} or {@link #failReads(int, ReadFailedException)}.
     */
    void unfailReads();

    /**
     * Resets any earlier {@link #failCommits(TransactionCommitFailedException)} or
     * {@link #failCommits(int, TransactionCommitFailedException)}.
     */
    void unfailCommits();
}
