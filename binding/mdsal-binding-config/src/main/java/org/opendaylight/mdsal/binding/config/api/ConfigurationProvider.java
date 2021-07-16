/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.config.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataRoot;

@Beta
@NonNullByDefault
@FunctionalInterface
public interface ConfigurationProvider<T extends ChildOf<? super DataRoot>> {

    ListenableFuture<T> requestValue();

    static <T extends ChildOf<? super DataRoot>> ConfigurationProvider<T> of(final T value) {
        final T checked = requireNonNull(value);
        return () -> Futures.immediateFuture(checked);
    }
}
