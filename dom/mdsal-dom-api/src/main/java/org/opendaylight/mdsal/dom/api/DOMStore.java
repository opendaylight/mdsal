/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.security.Principal;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.dom.api.query.DOMQuery;
import org.opendaylight.mdsal.dom.api.query.DOMQueryResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 *
 */
public interface DOMStore {
    /**
     * An access {@link DOMStore}. It needs to be {@link #enable(AccessSupervisor)}d before first use and can be
     * {@link #revoke(Exception)}d at any time.
     */
    @NonNullByDefault
    sealed interface Access permits ReadAccess, WriteAccess {
        /**
         * {@return the {@link Principal} of this access.
         */
        @Nullable Principal principal();

        /**
         * {@return the top-most path of this access is confined to}
         */
        @Nullable YangInstanceIdentifier confinement();

        /**
         * Enable this access, reporting any events to the specified {@link AccessSupervisor}.
         *
         * @param supervisor the {@link AccessSupervisor}
         * @throws IllegalStateException if this access was already enabled
         */
        void enable(AccessSupervisor supervisor);

        /**
         * Disable this access, reporting specified cause as the reason for revocation. If no cause is specified, then
         * supervisor will see a generic cause.
         *
         * <p>This operation is idempotent and safe to call from multiple threads concurrently, performing a one-time
         * transition:
         * <ul>
         *   <li>if this access was {@link #enable(AccessSupervisor)}d, the supervisor will be notified of the
         *       revocation and no further access
         *   <li>if this access was not {@link #enable(AccessSupervisor)}d, once an attempt is made, the provided
         *       supervisor will be notified about the revocation and the {@code enable()} will keep the access
         *       revoked</li>
         *   <li>if this access was previously {@link #revoke(Exception)}s, the invocation does nothing</li>
         * </ul>
         *
         * @param cause optional cause to report to the {@link AccessSupervisor}
         */
        void revoke(@Nullable Exception cause);
    }

    @NonNullByDefault
    non-sealed interface ReadAccess extends Access {

        ReadTransaction newReadTransaction();
    }

    @NonNullByDefault
    non-sealed interface WriteAccess extends Access {

        WriteTransaction newWriteTransaction();
    }

    @NonNullByDefault
    interface ReadWriteAccess extends ReadAccess, WriteAccess {

        ReadWriteTransaction newReadWriteTransaction();
    }

    @NonNullByDefault
    interface AccessSupervisor {
        /**
         * Invoked when an {@link Access} is revoked.
         *
         * @param access the {@link Access}
         * @param cause the cause for revocation
         */
        void onAccessRevoked(Access access, Exception cause);
    }

    /**
     * An independent, isolated {@link DOMStore} transaction.
     */
    sealed interface Transaction extends AutoCloseable permits ReadTransaction, WriteTransaction {
        /**
         * {@inheritDoc}
         *
         * This method is idempotent, this throw {@link IllegalStateException} on most of its invocations.
         */
        @Override
        void close();
    }

    /**
     * A {@link Transaction} providing {@link ReadOperations}.
     */
    non-sealed interface ReadTransaction extends Transaction, ReadOperations {
        // nothing else
    }

    /**
     * A {@link Transaction} providing {@link WriteOperations}.
     */
    non-sealed interface WriteTransaction extends Transaction, WriteOperations {
        @Override
        default void close() {
            cancel();
        }

        /**
         * Try to cancel this transaction, preventing any further access.
         *
         * <p>This method is idempotent:
         * <ul>
         *   <li>if this transaction has been aborted, this method does nothing and returns {@code false}</li>
         *   <li>if this transaction has been committed, this method does nothing and returns {@code false}</li>
         *   <li>if this transaction has failed to commit, this method does nothing and returns {@code false}</li>
         *   <li>if this transaction has started to commit, the implementation will try to cancel it and
         *     <ul>
         *       <li>if the cancellation succeeds, this method returns {@code true} and the commit callback will see
         *           a {@link CancellationException} failure</li>
         *       <li>if the cancellation fails, this method returns {@code false}</li>
         *     </ul>
         *   </li>
         *   <li>if this transaction has not started to commit, the implementation will free all free all underlying
         *       resources and return {@code true}<li>
         * </ul>
         *
         * <p>Once this method returns, attempts to use other methods, such as {@link #commit(BiConsumer)} with result
         * in {@link IllegalStateException} to be thrown.
         *
         * @return {@code true} if the transaction was aborted as a result of this call.
         */
        boolean cancel();

        void commit(@NonNull BiConsumer<CommitInfo, @Nullable Exception> callback);

        @NonNullByDefault
        default void commit(final Consumer<Exception> onFailure) {
            requireNonNull(onFailure);
            commit((unused, failure) -> {
                if (failure != null) {
                    onFailure.accept(failure);
                }
            });
        }

        @NonNullByDefault
        default void commit(final Consumer<CommitInfo> onSuccess, final Consumer<Exception> onFailure) {
            requireNonNull(onSuccess);
            requireNonNull(onFailure);
            commit((success, failure) -> {
                if (failure != null) {
                    onFailure.accept(failure);
                } else {
                    onSuccess.accept(verifyNotNull(success));
                }
            });
        }
    }

    /**
     * A {@link WriteTransaction} also providing {@link ReadOperations}.
     */
    @NonNullByDefault
    interface ReadWriteTransaction extends WriteTransaction, ReadOperations {
        // Nothing else
    }

    // FIXME: how does shutdown work?
    @NonNullByDefault
    interface WriteChain {

        // FIXME: how do failure/success backs work?
        void runWrite(Predicate<WriteOperations> operation);
    }

    @NonNullByDefault
    interface ReadWriteChain extends WriteChain {

        void runRead(Consumer<WriteOperations> operation);

        void runReadWrite(Predicate<ReadWriteOperations> operation);
    }

    /**
     * Read-side operations of a {@link DOMStore}.
     */
    interface ReadOperations {
        /**
         * Reads data from provided logical data store located at provided path.
         *
         * @param path Path which uniquely identifies subtree which client want to read
         * @return a FluentFuture containing the result of the read. The Future blocks until the commit operation is
         *         complete. Once complete:
         *         <ul>
         *           <li>If the data at the supplied path exists, the Future returns an Optional object containing
         *               the data.</li>
         *           <li>If the data at the supplied path does not exist, the Future returns Optional.empty().</li>
         *           <li>If the read of the data fails, the Future will fail with a {@link ReadFailedException} or an
         *               exception derived from ReadFailedException.</li>
         *         </ul>
         */
        @NonNull FluentFuture<Optional<NormalizedNode>> read(@NonNull YangInstanceIdentifier path);

        /**
         * Checks if data is available in the logical data store located at provided path.
         *
         * <p>Note: a successful result from this method makes no guarantee that a subsequent call to {@link #read} will
         * succeed. It is possible that the data resides in a data store on a remote node and, if that node goes down or
         * a network failure occurs, a subsequent read would fail. Another scenario is if the data is deleted in between
         * the calls to {@code exists} and {@code>read}.
         *
         * <p>Default implementation defers to {@link #read(YangInstanceIdentifier)}.
         *
         * @param path Path which uniquely identifies subtree which client want to check existence of
         * @return a FluentFuture containing the result of the check.
         *         <ul>
         *           <li>If the data at the supplied path exists, the Future returns a Boolean whose value is true, false
         *               otherwise</li>
         *           <li>If checking for the data fails, the Future will fail with a {@link ReadFailedException} or an
         *               exception derived from ReadFailedException.</li>
         *         </ul>
         */
        default @NonNull FluentFuture<Boolean> exists(final @NonNull YangInstanceIdentifier path) {
            return read(path).transform(Optional::isPresent, MoreExecutors.directExecutor());
        }

        /**
         * Executes a query on the provided logical data store.
         *
         * @param query DOMQuery to execute
         * @return a FluentFuture containing the result of the query. The Future blocks until the operation is complete.
         *         Once complete:
         *         <ul>
         *           <li>The Future returns the result of the query</li>
         *           <li>If the query execution fails, the Future will fail with a {@link ReadFailedException} or
         *               an exception derived from ReadFailedException.</li>
         *         </ul>
         * @throws NullPointerException if any of the arguments is null
         * @throws IllegalArgumentException if the query is not supported
         */
        @NonNull FluentFuture<DOMQueryResult> execute(@NonNull DOMQuery query);
    }

    /**
     * Writer-side operations of a {@link DOMStore}.
     */
    @NonNullByDefault
    interface WriteOperations {
        /**
         * Store a provided data at specified path. This acts as a add / replace operation, which is to
         * say that whole subtree will be replaced by specified path.
         *
         * <p>If you need add or merge of current object with specified use
         * {@link #merge(YangInstanceIdentifier, NormalizedNode)}
         *
         * @param path YangInstanceIdentifier object to be written
         * @param data Data object to be written
         * @throws IllegalStateException if the the store cannot be accessed
         */
        void write(YangInstanceIdentifier path, NormalizedNode data);

        /**
         * Store a provided data at specified path. This acts as a add / replace operation, which is to
         * say that whole subtree will be replaced by specified path.
         *
         * <p>If you need add or merge of current object with specified use
         * {@link #merge(YangInstanceIdentifier, NormalizedNode)}
         *
         * @param path YangInstanceIdentifier object to be merged
         * @param data Data object to be written
         * @throws IllegalStateException if the the store cannot be accessed
         */
        void merge(YangInstanceIdentifier path, NormalizedNode data);

        /**
         * Deletes data and whole subtree located at provided path.
         *
         * @param path Path to delete
         * @throws IllegalStateException if the the store cannot be accessed
         */
        void delete(YangInstanceIdentifier path);
    }

    interface ReadWriteOperations extends ReadOperations, WriteOperations {
        // Nothing else
    }

    @NonNullByDefault
    ReadAccess newReadAccess(Executor executor, @Nullable YangInstanceIdentifier confinement,
        @Nullable Principal principal);

    @NonNullByDefault
    default ReadAccess newReadAccess() {
        return newReadAccess(MoreExecutors.directExecutor());
    }

    @NonNullByDefault
    default ReadAccess newReadAccess(final @Nullable Principal principal) {
        return newReadAccess((YangInstanceIdentifier) null, principal);
    }

    @NonNullByDefault
    default ReadAccess newReadAccess(final @Nullable YangInstanceIdentifier confinement) {
        return newReadAccess(confinement, null);
    }

    @NonNullByDefault
    default ReadAccess newReadAccess(final @Nullable YangInstanceIdentifier confinement,
            final @Nullable Principal principal) {
        return newReadAccess(MoreExecutors.directExecutor(), confinement, principal);
    }

    @NonNullByDefault
    default ReadAccess newReadAccess(final Executor executor) {
        return newReadAccess(executor, null, null);
    }

    @NonNullByDefault
    default ReadAccess newReadAccess(final Executor executor, final @Nullable Principal principal) {
        return newReadAccess(executor, null, principal);
    }

    @NonNullByDefault
    default ReadAccess newReadAccess(final Executor executor, final @Nullable YangInstanceIdentifier confinement) {
        return newReadAccess(executor, confinement, null);
    }

    @NonNullByDefault
    WriteAccess newWriteAccess(Executor executor, @Nullable YangInstanceIdentifier confinement,
        @Nullable Principal principal);

    @NonNullByDefault
    default WriteAccess newWriteAccess() {
        return newWriteAccess(MoreExecutors.directExecutor());
    }

    @NonNullByDefault
    default WriteAccess newWriteAccess(final @Nullable Principal principal) {
        return newWriteAccess((YangInstanceIdentifier) null, principal);
    }

    @NonNullByDefault
    default WriteAccess newWriteAccess(final @Nullable YangInstanceIdentifier confinement) {
        return newWriteAccess(confinement, null);
    }

    @NonNullByDefault
    default WriteAccess newWriteAccess(final @Nullable YangInstanceIdentifier confinement,
            final @Nullable Principal principal) {
        return newWriteAccess(MoreExecutors.directExecutor(), confinement, principal);
    }

    @NonNullByDefault
    default WriteAccess newWriteAccess(final Executor executor) {
        return newWriteAccess(executor, null, null);
    }

    @NonNullByDefault
    default WriteAccess newWriteAccess(final Executor executor, final @Nullable Principal principal) {
        return newWriteAccess(executor, null, principal);
    }

    @NonNullByDefault
    default WriteAccess newWriteAccess(final Executor executor, final @Nullable YangInstanceIdentifier confinement) {
        return newWriteAccess(executor, confinement, null);
    }
}
