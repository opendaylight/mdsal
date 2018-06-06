/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.type.util;

import com.google.common.annotations.Beta;
import java.net.InetAddress;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Common interface for objects which can act as an {@link InetAddress}. Implementations of this interface are required
 * to be immutable.
 */
@Beta
@NonNullByDefault
public interface InetAddressLike extends Immutable {
    /**
     * Return an {@link InetAddress} representation of this object.
     *
     * @return an {@link InetAddress} representation of this object.
     */
    InetAddress toInetAddress();
}
