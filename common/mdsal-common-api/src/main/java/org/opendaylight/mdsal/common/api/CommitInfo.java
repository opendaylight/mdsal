/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FluentFuture;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base interface for tagging information about a successful commit. This can include various ways of identifying
 * the resulting changeset, timing information or any other piece of data about the commit itself the implementation
 * deems interesting to the client.
 */
@NonNullByDefault
public interface CommitInfo extends Serializable {
    /**
     * The {@link UUID} of the commit.
     *
     * @return the {@link UUID} of this commit
     */
    @Nullable UUID uuid();

    /**
     * The {@link Instant} when the commit occurred.
     *
     * @return the {@link Instant} when the commit occurred
     */
    @Nullable Instant instant();

    /**
     * Return an empty {@link CommitInfo}.
     *
     * @return An empty {@link CommitInfo} instance.
     */
    static CommitInfo empty() {
        return CI.EMPTY;
    }

    /**
     * Return an immediately-completed empty {@link CommitInfo} future.
     *
     * @return An empty {@link CommitInfo} instance enclosed in a completed future.
     */
    static FluentFuture<CommitInfo> emptyFluentFuture() {
        return CI.EMPTY_FUTURE;
    }

    /**
     * Return a {@link CommitInfo} reporting specified {@link UUID}.
     *
     * @param uuid UUID to report
     * @return a {@link CommitInfo} reporting specified {@link UUID}
     * @throws NullPointerException if {@code uuid} is {@code null}
     */
    static CommitInfo of(final UUID uuid) {
        return new CI(requireNonNull(uuid), null);
    }

    /**
     * Return a {@link CommitInfo} reporting specified {@link Instant}.
     *
     * @param instant Instant to report
     * @return a {@link CommitInfo} reporting specified {@link Instant}
     * @throws NullPointerException if {@code instant} is {@code null}
     */
    static CommitInfo of(final Instant instant) {
        return new CI(null, requireNonNull(instant));
    }

    /**
     * Return a {@link CommitInfo} reporting specified {@link UUID} and {@link Instant}.
     *
     * @param uuid UUID to report
     * @param instant Instant to report
     * @return a {@link CommitInfo} reporting specified {@link UUID} and {@link Instant}
     * @throws NullPointerException if any argument is {@code null}
     */
    static CommitInfo of(final UUID uuid, final Instant instant) {
        return new CI(requireNonNull(uuid), requireNonNull(instant));
    }

    /**
     * Return a {@link CommitInfo} reporting optional {@link UUID} and  {@link Instant}.
     *
     * @param uuid UUID to report
     * @param instant Instant to report
     * @return a {@link CommitInfo} reporting optional {@link UUID} and  {@link Instant}
     */
    static CommitInfo ofNullable(final @Nullable UUID uuid, final @Nullable Instant instant) {
        return CI.of(uuid, instant);
    }
}
