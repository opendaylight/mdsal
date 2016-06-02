/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.api;

import com.google.common.util.concurrent.CheckedFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.yangtools.concepts.Identifiable;

public interface DOMDataTreeCursorAwareTransaction extends DOMDataTreeCursorProvider, Identifiable<String> {

    @Nullable
    @Override
    DOMDataTreeWriteCursor createCursor(@Nonnull DOMDataTreeIdentifier path);

    boolean cancel();

    CheckedFuture<Void,TransactionCommitFailedException> submit();

}
