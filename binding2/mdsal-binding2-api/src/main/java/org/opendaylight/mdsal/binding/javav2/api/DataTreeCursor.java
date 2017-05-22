/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;


/**
 * A cursor holding a logical position within a conceptual data tree. It allows operations relative
 * to that position, as well as moving the position up or down the tree.
 */
@Beta
@NotThreadSafe
public interface DataTreeCursor extends AutoCloseable {

    void enter(@Nonnull TreeArgument<?> child);

    void enter(@Nonnull TreeArgument<?>... path);

    void enter(@Nonnull Iterable<TreeArgument<?>> path);

    void exit();

    void exit(int depth);

    @Override
    void close() throws Exception;
}
