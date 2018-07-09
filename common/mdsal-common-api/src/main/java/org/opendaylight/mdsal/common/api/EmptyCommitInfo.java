/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import com.google.common.util.concurrent.FluentFuture;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;

/**
 * Empty commit info singleton. Useful when {@link AsyncWriteTransaction#commit()} has nothing more to say.
 *
 * @author Robert Varga
 */
@NonNullByDefault
final class EmptyCommitInfo implements CommitInfo {
    static final CommitInfo INSTANCE = new EmptyCommitInfo();
    static final FluentFuture<CommitInfo> FLUENT_INSTANCE = FluentFutures.immediateFluentFuture(INSTANCE);

    private EmptyCommitInfo() {
        // Hidden
    }
}
