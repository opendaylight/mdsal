/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FluentFuture;
import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.DataValidationFailedException;
import org.opendaylight.mdsal.common.api.ForwardingOnCommitFutureCallback;
import org.opendaylight.mdsal.common.api.OnCommitCallback;
import org.opendaylight.mdsal.common.api.OnCommitFutureCallback;
import org.opendaylight.mdsal.common.api.OptimisticLockFailedException;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;

/**
 * Write transaction provides mutation capabilities for a data tree.
 *
 * <p>Initial state of write transaction is a stable snapshot of the current data tree. The state is captured when
 * the transaction is created and its state and underlying data tree are not affected by other concurrently running
 * transactions.
 *
 * <p>Write transactions are isolated from other concurrent write transactions. All writes are local to the transaction
 * and represent only a proposal of state change for the data tree and it is not visible to any other concurrently
 * running transaction.
 *
 * <p>Applications make changes to the local data tree in the transaction by via the <b>put</b>, <b>merge</b>, and
 * <b>delete</b> operations.
 *
 * <h2>Put operation</h2>
 * Stores a piece of data at a specified path. This acts as an add / replace operation, which is to say that whole
 * subtree will be replaced by the specified data.
 *
 * <p>Performing the following put operations:
 *
 * <pre>
 * 1) container { list [ a ] }
 * 2) container { list [ b ] }
 * </pre>
 * will result in the following data being present:
 *
 * <pre>
 * container { list [ b ] }
 * </pre>
 * <h2>Merge operation</h2>
 * Merges a piece of data with the existing data at a specified path. Any pre-existing data which is not explicitly
 * overwritten will be preserved. This means that if you store a container, its child lists will be merged.
 *
 * <p>Performing the following merge operations:
 *
 * <pre>
 * 1) container { list [ a ] }
 * 2) container { list [ b ] }
 * </pre>
 * will result in the following data being present:
 *
 * <pre>
 * container { list [ a, b ] }
 * </pre>
 * This also means that storing the container will preserve any augmentations which have been attached to it.
 *
 * <h2>Delete operation</h2>
 * Removes a piece of data from a specified path.
 *
 * <p>After applying changes to the local data tree, applications publish the changes proposed in the transaction
 * by calling {@link #commit} on the transaction. This seals the transaction (preventing any further writes using
 * this transaction) and commits it to be processed and applied to global conceptual data tree.
 *
 * <p>The transaction commit may fail due to a concurrent transaction modifying and committing data in an incompatible
 * way. See {@link #commit} for more concrete commit failure examples.
 *
 * <p><b>Implementation Note:</b> This interface is not intended to be implemented by users of MD-SAL, but only to be
 * consumed by them.
 */
public interface DOMDataTreeWriteTransaction extends DOMDataTreeTransaction, DOMDataTreeWriteOperations {
    /**
     * Commits this transaction to be asynchronously applied to update the logical data tree. The returned
     * {@link FluentFuture} conveys the result of applying the data changes.
     *
     * <p>This call logically seals the transaction, which prevents the client from further changing the data tree using
     * this transaction. Any subsequent calls to <code>put(LogicalDatastoreType, Path, Object)</code>,
     * <code>merge(LogicalDatastoreType, Path, Object)</code>, <code>delete(LogicalDatastoreType, Path)</code> will fail
     * with {@link IllegalStateException}. The transaction is marked as committed and enqueued into the data store
     * back-end for processing.
     *
     * <p>Whether or not the commit is successful is determined by versioning of the data tree and validation of
     * registered commit participants if the transaction changes the data tree.
     *
     * <p>The effects of a successful commit of data depends on listeners and commit participants that are registered
     * with the data broker.
     *
     * <h4>Example usage:</h4>
     * <pre>
     *  private void doWrite(final int tries) {
     *      WriteTransaction writeTx = dataBroker.newWriteOnlyTransaction();
     *      MyDataObject data = ...;
     *      InstanceIdentifier&lt;MyDataObject&gt; path = ...;
     *      writeTx.put(LogicalDatastoreType.OPERATIONAL, path, data);
     *      Futures.addCallback(writeTx.commit(), new FutureCallback&lt;CommitInfo&gt;() {
     *          public void onSuccess(CommitInfo result) {
     *              // succeeded
     *          }
     *          public void onFailure(Throwable t) {
     *              if (t instanceof OptimisticLockFailedException) {
     *                  if(( tries - 1) &gt; 0 ) {
     *                      // do retry
     *                      doWrite(tries - 1);
     *                  } else {
     *                      // out of retries
     *                  }
     *              } else {
     *                  // failed due to another type of TransactionCommitFailedException.
     *              }
     *          });
     * }
     * ...
     * doWrite(2);
     * </pre>
     *
     * <h4>Failure scenarios</h4>
     *
     * <p>Transaction may fail because of multiple reasons, such as
     * <ul>
     *   <li>
     *     Another transaction finished earlier and modified the same node in a non-compatible way (see below). In this
     *     case the returned future will fail with an {@link OptimisticLockFailedException}. It is the responsibility
     *     of the caller to create a new transaction and commit the same modification again in order to update data
     *     tree.
     *     <i>
     *       <b>Warning</b>: In most cases, retrying after an OptimisticLockFailedException will result in a high
     *       probability of success. However, there are scenarios, albeit unusual, where any number of retries will
     *       not succeed. Therefore it is strongly recommended to limit the number of retries (2 or 3) to avoid
     *       an endless loop.
     *     </i>
     *   </li>
     *   <li>Data change introduced by this transaction did not pass validation by commit handlers or data was
     *       incorrectly structured. Returned future will fail with a {@link DataValidationFailedException}. User
     *       should not retry to create new transaction with same data, since it probably will fail again.
     *   </li>
     * </ul>
     *
     * <h4>Change compatibility</h4>
     * There are several sets of changes which could be considered incompatible between two transactions which are
     * derived from same initial state. Rules for conflict detection applies recursively for each subtree level.
     *
     * <h4>Change compatibility of leafs, leaf-list items</h4>
     * Following table shows state changes and failures between two concurrent transactions, which are based on same
     * initial state, Tx 1 completes successfully before Tx 2 is committed.
     *
     * <table>
     * <caption>Change compatibility of leaf values</caption>
     * <tr>
     * <th>Initial state</th>
     * <th>Tx 1</th>
     * <th>Tx 2</th>
     * <th>Result</th>
     * </tr>
     * <tr>
     * <td>Empty</td>
     * <td>put(A,1)</td>
     * <td>put(A,2)</td>
     * <td>Tx 2 will fail, state is A=1</td>
     * </tr>
     * <tr>
     * <td>Empty</td>
     * <td>put(A,1)</td>
     * <td>merge(A,2)</td>
     * <td>A=2</td>
     * </tr>
     *
     * <tr>
     * <td>Empty</td>
     * <td>merge(A,1)</td>
     * <td>put(A,2)</td>
     * <td>Tx 2 will fail, state is A=1</td>
     * </tr>
     * <tr>
     * <td>Empty</td>
     * <td>merge(A,1)</td>
     * <td>merge(A,2)</td>
     * <td>A=2</td>
     * </tr>
     *
     *
     * <tr>
     * <td>A=0</td>
     * <td>put(A,1)</td>
     * <td>put(A,2)</td>
     * <td>Tx 2 will fail, A=1</td>
     * </tr>
     * <tr>
     * <td>A=0</td>
     * <td>put(A,1)</td>
     * <td>merge(A,2)</td>
     * <td>A=2</td>
     * </tr>
     * <tr>
     * <td>A=0</td>
     * <td>merge(A,1)</td>
     * <td>put(A,2)</td>
     * <td>Tx 2 will fail, A=1</td>
     * </tr>
     * <tr>
     * <td>A=0</td>
     * <td>merge(A,1)</td>
     * <td>merge(A,2)</td>
     * <td>A=2</td>
     * </tr>
     *
     * <tr>
     * <td>A=0</td>
     * <td>delete(A)</td>
     * <td>put(A,2)</td>
     * <td>Tx 2 will fail, A does not exists</td>
     * </tr>
     * <tr>
     * <td>A=0</td>
     * <td>delete(A)</td>
     * <td>merge(A,2)</td>
     * <td>A=2</td>
     * </tr>
     * </table>
     *
     * <h4>Change compatibility of subtrees</h4>
     * Following table shows state changes and failures between two concurrent transactions, which are based on same
     * initial state, Tx 1 completes successfully before Tx 2 is committed.
     *
     * <table>
     * <caption>Change compatibility of containers</caption>
     * <tr>
     * <th>Initial state</th>
     * <th>Tx 1</th>
     * <th>Tx 2</th>
     * <th>Result</th>
     * </tr>
     *
     * <tr>
     * <td>Empty</td>
     * <td>put(TOP,[])</td>
     * <td>put(TOP,[])</td>
     * <td>Tx 2 will fail, state is TOP=[]</td>
     * </tr>
     * <tr>
     * <td>Empty</td>
     * <td>put(TOP,[])</td>
     * <td>merge(TOP,[])</td>
     * <td>TOP=[]</td>
     * </tr>
     *
     * <tr>
     * <td>Empty</td>
     * <td>put(TOP,[FOO=1])</td>
     * <td>put(TOP,[BAR=1])</td>
     * <td>Tx 2 will fail, state is TOP=[FOO=1]</td>
     * </tr>
     * <tr>
     * <td>Empty</td>
     * <td>put(TOP,[FOO=1])</td>
     * <td>merge(TOP,[BAR=1])</td>
     * <td>TOP=[FOO=1,BAR=1]</td>
     * </tr>
     *
     * <tr>
     * <td>Empty</td>
     * <td>merge(TOP,[FOO=1])</td>
     * <td>put(TOP,[BAR=1])</td>
     * <td>Tx 2 will fail, state is TOP=[FOO=1]</td>
     * </tr>
     * <tr>
     * <td>Empty</td>
     * <td>merge(TOP,[FOO=1])</td>
     * <td>merge(TOP,[BAR=1])</td>
     * <td>TOP=[FOO=1,BAR=1]</td>
     * </tr>
     *
     * <tr>
     * <td>TOP=[]</td>
     * <td>put(TOP,[FOO=1])</td>
     * <td>put(TOP,[BAR=1])</td>
     * <td>Tx 2 will fail, state is TOP=[FOO=1]</td>
     * </tr>
     * <tr>
     * <td>TOP=[]</td>
     * <td>put(TOP,[FOO=1])</td>
     * <td>merge(TOP,[BAR=1])</td>
     * <td>state is TOP=[FOO=1,BAR=1]</td>
     * </tr>
     * <tr>
     * <td>TOP=[]</td>
     * <td>merge(TOP,[FOO=1])</td>
     * <td>put(TOP,[BAR=1])</td>
     * <td>Tx 2 will fail, state is TOP=[FOO=1]</td>
     * </tr>
     * <tr>
     * <td>TOP=[]</td>
     * <td>merge(TOP,[FOO=1])</td>
     * <td>merge(TOP,[BAR=1])</td>
     * <td>state is TOP=[FOO=1,BAR=1]</td>
     * </tr>
     * <tr>
     * <td>TOP=[]</td>
     * <td>delete(TOP)</td>
     * <td>put(TOP,[BAR=1])</td>
     * <td>Tx 2 will fail, state is empty store</td>
     * </tr>
     * <tr>
     * <td>TOP=[]</td>
     * <td>delete(TOP)</td>
     * <td>merge(TOP,[BAR=1])</td>
     * <td>state is TOP=[BAR=1]</td>
     * </tr>
     *
     * <tr>
     * <td>TOP=[]</td>
     * <td>put(TOP/FOO,1)</td>
     * <td>put(TOP/BAR,1])</td>
     * <td>state is TOP=[FOO=1,BAR=1]</td>
     * </tr>
     * <tr>
     * <td>TOP=[]</td>
     * <td>put(TOP/FOO,1)</td>
     * <td>merge(TOP/BAR,1)</td>
     * <td>state is TOP=[FOO=1,BAR=1]</td>
     * </tr>
     * <tr>
     * <td>TOP=[]</td>
     * <td>merge(TOP/FOO,1)</td>
     * <td>put(TOP/BAR,1)</td>
     * <td>state is TOP=[FOO=1,BAR=1]</td>
     * </tr>
     * <tr>
     * <td>TOP=[]</td>
     * <td>merge(TOP/FOO,1)</td>
     * <td>merge(TOP/BAR,1)</td>
     * <td>state is TOP=[FOO=1,BAR=1]</td>
     * </tr>
     * <tr>
     * <td>TOP=[]</td>
     * <td>delete(TOP)</td>
     * <td>put(TOP/BAR,1)</td>
     * <td>Tx 2 will fail, state is empty store</td>
     * </tr>
     * <tr>
     * <td>TOP=[]</td>
     * <td>delete(TOP)</td>
     * <td>merge(TOP/BAR,1]</td>
     * <td>Tx 2 will fail, state is empty store</td>
     * </tr>
     *
     * <tr>
     * <td>TOP=[FOO=1]</td>
     * <td>put(TOP/FOO,2)</td>
     * <td>put(TOP/BAR,1)</td>
     * <td>state is TOP=[FOO=2,BAR=1]</td>
     * </tr>
     * <tr>
     * <td>TOP=[FOO=1]</td>
     * <td>put(TOP/FOO,2)</td>
     * <td>merge(TOP/BAR,1)</td>
     * <td>state is TOP=[FOO=2,BAR=1]</td>
     * </tr>
     * <tr>
     * <td>TOP=[FOO=1]</td>
     * <td>merge(TOP/FOO,2)</td>
     * <td>put(TOP/BAR,1)</td>
     * <td>state is TOP=[FOO=2,BAR=1]</td>
     * </tr>
     * <tr>
     * <td>TOP=[FOO=1]</td>
     * <td>merge(TOP/FOO,2)</td>
     * <td>merge(TOP/BAR,1)</td>
     * <td>state is TOP=[FOO=2,BAR=1]</td>
     * </tr>
     * <tr>
     * <td>TOP=[FOO=1]</td>
     * <td>delete(TOP/FOO)</td>
     * <td>put(TOP/BAR,1)</td>
     * <td>state is TOP=[BAR=1]</td>
     * </tr>
     * <tr>
     * <td>TOP=[FOO=1]</td>
     * <td>delete(TOP/FOO)</td>
     * <td>merge(TOP/BAR,1]</td>
     * <td>state is TOP=[BAR=1]</td>
     * </tr>
     * </table>
     *
     *
     * <h4>Examples of failure scenarios</h4>
     *
     * <h5>Conflict of two transactions</h5>
     * This example illustrates two concurrent transactions, which derived from same initial state
     * of data tree and proposes conflicting modifications.
     *
     * <pre>
     * txA = broker.newWriteTransaction(); // allocates new transaction, data tree is empty
     * txB = broker.newWriteTransaction(); // allocates new transaction, data tree is empty
     * txA.put(CONFIGURATION, PATH, A);    // writes to PATH value A
     * txB.put(CONFIGURATION, PATH, B)     // writes to PATH value B
     * ListenableFuture futureA = txA.commit(); // transaction A is sealed and committed
     * ListenebleFuture futureB = txB.commit(); // transaction B is sealed and committed
     * </pre>
     * Commit of transaction A will be processed asynchronously and data tree will be updated to
     * contain value <code>A</code> for <code>PATH</code>. Returned {@link FluentFuture} will
     * successfully complete once state is applied to data tree.
     * Commit of Transaction B will fail, because previous transaction also modified path in a
     * concurrent way. The state introduced by transaction B will not be applied. Returned
     * {@link FluentFuture} object will fail with {@link OptimisticLockFailedException}
     * exception, which indicates to client that concurrent transaction prevented the committed
     * transaction from being applied. <br>
     *
     * <p>A successful commit produces implementation-specific {@link CommitInfo} structure, which is used to
     * communicate post-condition information to the caller. Such information can contain commit-id, timing information
     * or any other information the implementation wishes to share.
     *
     * @return a FluentFuture containing the result of the commit information. The Future blocks until the commit
     *         operation is complete. A successful commit returns nothing. On failure, the Future will fail with a
     *         {@link TransactionCommitFailedException} or an exception derived from TransactionCommitFailedException.
     * @throws IllegalStateException if the transaction is already committed or was cancelled.
     */
    @CheckReturnValue
    @NonNull FluentFuture<? extends @NonNull CommitInfo> commit();

    /**
     * Commit this tranaction and invoke specified callback when it completes using the specified executor.
     *
     * @param callback {@link OnCommitCallback} to invoke
     * @param executor {@link Executor} to use
     * @throws IllegalStateException if the transaction is already committed or was cancelled.
     * @throws NullPointerException if any argument is {@code null}
     */
    default void commit(final @NonNull OnCommitCallback callback, final @NonNull Executor executor) {
        final var futureCallback = callback instanceof OnCommitFutureCallback onCommit ? onCommit
            : new ForwardingOnCommitFutureCallback(callback);
        requireNonNull(executor);
        commit().addCallback(futureCallback, executor);
    }

    /**
     * Cancels the transaction. Transactions can only be cancelled if it was not yet committed.
     * Invoking cancel() on failed or already cancelled will have no effect, and transaction is considered cancelled.
     * Invoking cancel() on finished transaction (future returned by {@link #commit()} already successfully completed)
     * will always fail (return false).
     *
     * @return {@code false} if the task could not be cancelled, typically because it has already completed normally;
     *         {@code true} otherwise
     */
    boolean cancel();
}
