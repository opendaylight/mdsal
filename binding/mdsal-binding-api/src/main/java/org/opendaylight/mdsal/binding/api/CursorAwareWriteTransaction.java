/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.api;

import com.google.common.util.concurrent.CheckedFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.binding.DataObject;

public interface CursorAwareWriteTransaction extends DataTreeCursorProvider, AutoCloseable, Identifiable<String> {

    @Nullable
    @Override
    <T extends DataObject> DataTreeWriteCursor createCursor(@Nonnull DataTreeIdentifier<T> path);

    boolean cancel();

    CheckedFuture<Void,TransactionCommitFailedException> submit();
}
