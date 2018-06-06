/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Common interface for objects which can act as a {@code byte[]}. Implementations of this interface are required
 * to be immutable.
 *
 * @author Robert Varga
 * @deprecated This is a preview API. Do not use yet.
 */
@Beta
@Deprecated
@NonNullByDefault
public interface ByteArrayLike extends Immutable {
    /**
     * Return the byte at specified offset.
     *
     * @param offset Offset of the byte
     * @return Byte value at specified offset
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    byte getByteAt(int offset);

    /**
     * Return the logical {@code byte[].length} of this object.
     *
     * @return Logical byte array length
     */
    int getLength();

    /**
     * Return the byte array representation of this object.
     *
     * @return Byte array representation of this object.
     */
    byte[] toByteArray();
}
