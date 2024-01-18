/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Data Broker which provides data transaction and data change listener functionality using {@link NormalizedNode}.
 *
 * <p>
 * All operations on the data tree are performed via one of the transactions:
 * <ul>
 * <li>Read-Only - allocated using {@link #newReadOnlyTransaction()}
 * <li>Write-Only - allocated using {@link #newWriteOnlyTransaction()}
 * </ul>
 *
 * <p>
 * These transactions provide a stable isolated view of data tree, which is guaranteed to be not
 * affected by other concurrent transactions, until transaction is committed.
 *
 * <p>
 * For a detailed explanation of how transaction are isolated and how transaction-local changes are
 * committed to global data tree, see {@link DOMDataTreeReadTransaction}, {@link DOMDataTreeWriteTransaction}
 * and {@link DOMDataTreeWriteTransaction#commit()}.
 *
 *
 * <p>
 * It is strongly recommended to use the type of transaction, which provides only the minimal
 * capabilities you need. This allows for optimizations at the data broker / data store level. For
 * example, implementations may optimize the transaction for reading if they know ahead of time that
 * you only need to read data - such as not keeping additional meta-data, which may be required for
 * write transactions.
 *
 * <p>
 * <b>Implementation Note:</b> This interface is not intended to be implemented by users of MD-SAL,
 * but only to be consumed by them.
 */
@NonNullByDefault
public interface DOMDataBroker extends DOMService<DOMDataBroker, DOMDataBroker.Extension>, DOMTransactionFactory {
    /**
     * Type capture of a {@link DOMService.Extension} applicable to {@link DOMDataBroker} implementations.
     */
    interface Extension extends DOMService.Extension<DOMDataBroker, Extension> {
        // Marker interface
    }

    /**
     * Create a new transaction chain. The chain will be initialized to read from its backing datastore, with
     * no outstanding transaction.
     *
     * @return A new transaction chain.
     */
    DOMTransactionChain createTransactionChain();

    /**
     * Create a new transaction chain. The chain will be initialized to read from its backing datastore, with
     * no outstanding transaction.
     *
     * <p>
     * Unlike {@link #createTransactionChain()}, the transaction chain returned by this method is allowed to merge
     * individual transactions into larger chunks. When transactions are merged, the results must be indistinguishable
     * from the result of all operations having been performed on a single transaction.
     *
     * <p>
     * When transactions are merged, {@link DOMTransactionChain#newReadOnlyTransaction()} may actually be backed by
     * a read-write transaction, hence an additional restriction on API use is that multiple read-only transactions
     * may not be open at the same time.
     *
     * @return A new transaction chain.
     */
    DOMTransactionChain createMergingTransactionChain();

    /**
     * Optional support for allowing a {@link DOMDataTreeCommitCohort} to participate in the process of committing
     * {@link DOMDataTreeWriteTransaction}s.
     */
    interface CommitCohortExtension extends Extension {
        /**
         * Register commit cohort which will participate in three-phase commit protocols of
         * {@link DOMDataTreeWriteTransaction} in data broker associated with this instance of extension.
         *
         * @param path Subtree path on which commit cohort operates.
         * @param cohort A {@link DOMDataTreeCommitCohort}
         * @return A {@link Registration}
         * @throws NullPointerException if any argument is {@code null}
         */
        Registration registerCommitCohort(DOMDataTreeIdentifier path, DOMDataTreeCommitCohort cohort);
    }

    /**
     * An {@link Extension} which allows users to register for changes to a subtree.
     */
    interface DataTreeChangeExtension extends Extension {
        /**
         * Registers a {@link DOMDataTreeChangeListener} to receive notifications when data changes under a given path
         * in the conceptual data tree.
         *
         * <p>
         * You are able to register for notifications for any node or subtree which can be represented using
         * {@link DOMDataTreeIdentifier}.
         *
         * <p>
         * You are able to register for data change notifications for a subtree or leaf even if it does not exist. You
         * will receive notification once that node is created.
         *
         * <p>
         * If there is any pre-existing data in the data tree for the path for which you are registering, you will
         * receive an initial data change event, which will contain all pre-existing data, marked as created.
         *
         * <p>
         * This method returns a {@link Registration} object. To "unregister" your listener for changes call
         * the {@link Registration#close()} method on the returned object.
         *
         * <p>
         * You MUST explicitly unregister your listener when you no longer want to receive notifications. This is
         * especially true in OSGi environments, where failure to do so during bundle shutdown can lead to stale
         * listeners being still registered.
         *
         * @param treeId Data tree identifier of the subtree which should be watched for changes.
         * @param listener Listener instance which is being registered
         * @return A {@link Registration} object, which may be used to unregister your listener using
         *         {@link Registration#close()} to stop delivery of change events.
         * @throws NullPointerException if any of the arguments is {@code null}
         */
        Registration registerDataTreeListener(DOMDataTreeIdentifier treeId, DOMDataTreeChangeListener listener);

        /**
         * Registers a {@link DOMDataTreeChangeListener} to receive notifications when data changes under a given path
         * in the conceptual data tree.
         *
         * <p>
         * You are able to register for notifications for any node or subtree which can be represented using
         * {@link DOMDataTreeIdentifier}.
         *
         * <p>
         * You are able to register for data change notifications for a subtree or leaf even if it does not exist. You
         * will receive notification once that node is created.
         *
         * <p>
         * If there is any pre-existing data in the data tree for the path for which you are registering, you will
         * receive an initial data change event, which will contain all pre-existing data, marked as created.
         *
         * <p>
         * This method returns a {@link Registration} object. To "unregister" your listener for changes call
         * the {@link Registration#close()} method on the returned object.
         *
         * <p>
         * You MUST explicitly unregister your listener when you no longer want to receive notifications. This is
         * especially true in OSGi environments, where failure to do so during bundle shutdown can lead to stale
         * listeners being still registered.
         *
         * @param treeId Data tree identifier of the subtree which should be watched for changes.
         * @param listener Listener instance which is being registered
         * @return A {@link Registration} object, which may be used to unregister your listener using
         *         {@link Registration#close()} to stop delivery of change events.
         * @throws NullPointerException if any of the arguments is {@code null}
         * @deprecated This interface relies on magic of {@link ClusteredDOMDataTreeChangeListener}. See
         *             {@link #registerLegacyDataTreeListener(DOMDataTreeIdentifier, DOMDataTreeChangeListener)} for
         *             migration guidance.
         */
        @Deprecated(since = "13.0.0", forRemoval = true)
        default Registration registerDataTreeChangeListener(final DOMDataTreeIdentifier treeId,
                final DOMDataTreeChangeListener listener) {
            return listener instanceof ClusteredDOMDataTreeChangeListener clustered
                ? registerDataTreeChangeListener(treeId, clustered) : registerLegacyDataTreeListener(treeId, listener);
        }

        /**
         * Registers a {@link ClusteredDOMDataTreeChangeListener} to receive notifications when data changes under a
         * given path in the conceptual data tree. This is a migration shorthand for
         * {@code registerDataTreeListener(treeId, listener)}.
         *
         * @param treeId Data tree identifier of the subtree which should be watched for changes.
         * @param listener Listener instance which is being registered
         * @return A {@link Registration} object, which may be used to unregister your listener using
         *         {@link Registration#close()} to stop delivery of change events.
         * @throws NullPointerException if any of the arguments is {@code null}
         * @deprecated Use {@link #registerDataTreeListener(DOMDataTreeIdentifier, DOMDataTreeChangeListener)} instead.
         */
        @Deprecated(since = "13.0.0", forRemoval = true)
        default Registration registerDataTreeChangeListener(final DOMDataTreeIdentifier treeId,
                final ClusteredDOMDataTreeChangeListener listener) {
            return registerDataTreeListener(treeId, listener);
        }

        /**
         * Registers a {@link DOMDataTreeChangeListener} to receive notifications when data changes under a given path
         * in the conceptual data tree, with legacy semantics, where no events are delivered if this "cluster node"
         * (further undefined) is a "leader" (also not explicitly undefined).
         *
         * <p>
         * The sole known implementation, the Akka-based datastore, defines the difference in terms of RAFT, suspending
         * even delivery when the RAFT leader is not local. Even when there may be valid use cases for this, RAFT there
         * is a storage backend whose lifecycle is disconnected from this object.
         *
         * <p>
         * Aside from the above difference, this method is equivalent to
         * {@link #registerDataTreeListener(DOMDataTreeIdentifier, DOMDataTreeChangeListener)}. If you are unable to
         * migrate, please contact us on <a href="email:discuss@lists.opendaylight.org">the mailing list</a>
         *
         * @param treeId Data tree identifier of the subtree which should be watched for changes.
         * @param listener Listener instance which is being registered
         * @return A {@link Registration} object, which may be used to unregister your listener using
         *         {@link Registration#close()} to stop delivery of change events.
         * @throws NullPointerException if any of the arguments is {@code null}
         */
        @Deprecated(since = "13.0.0", forRemoval = true)
        Registration registerLegacyDataTreeListener(DOMDataTreeIdentifier treeId, DOMDataTreeChangeListener listener);
    }
}
