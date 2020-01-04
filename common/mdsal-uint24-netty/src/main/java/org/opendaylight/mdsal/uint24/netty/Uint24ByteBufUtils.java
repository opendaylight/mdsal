/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.uint24.netty;

import com.google.common.annotations.Beta;
import io.netty.buffer.ByteBuf;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.uint24.rev200104.Uint24;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.netty.ByteBufUtils;

@Beta
public final class Uint24ByteBufUtils {
    private Uint24ByteBufUtils() {
        // Hidden on purpose
    }

    /**
     * Read a {@link Uint24} from specified buffer.
     *
     * @param buf buffer
     * @return A {@link Uint24}
     * @throws NullPointerException if {@code buf} is null
     * @throws IndexOutOfBoundsException if {@code buf} does not have enough data
     */
    public static @NonNull Uint24 readUint24(final ByteBuf buf) {
        return new Uint24(ByteBufUtils.readUint32(buf));
    }

    /**
     * Write a {@link Uint24} to specified buffer.
     *
     * @param buf buffer
     * @param value A {@link Uint24}
     * @throws NullPointerException if any argument is null
     */
    public static void writeUint24(final ByteBuf buf, final Uint24 value) {
        buf.writeMedium(value.getValue().intValue());
    }

    /**
     * Write a {@link Uint24} property to specified buffer. If the {@code value} is known to be non-null, prefer to use
     * {@link #writeUint24(ByteBuf, Uint24)} instead of this method.
     *
     * @param buf buffer
     * @param value A {@link Uint24}
     * @param name Property name for error reporting purposes
     * @throws NullPointerException if {@code buf} is null
     * @throws IllegalArgumentException if {@code value} is null
     */
    public static void writeMandatoryUint24(final ByteBuf buf, final Uint24 value, final String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is mandatory");
        }
        writeUint24(buf, value);
    }

    /**
     * Write a {@link Uint64} value to specified buffer if it is not null.
     *
     * @param buf buffer
     * @param value A {@link Uint64}
     * @throws NullPointerException if {@code buf} is null
     */
    public static void writeOptionalUint24(final ByteBuf buf, final @Nullable Uint24 value) {
        if (value != null) {
            writeUint24(buf, value);
        }
    }

    /**
     * Write a {@link Uint24} value to specified buffer if it is not null, otherwise write four zero bytes.
     *
     * @param buf buffer
     * @param value A {@link Uint24}
     * @throws NullPointerException if {@code buf} is null
     */
    public static void writeUint24OrZero(final ByteBuf buf, final @Nullable Uint24 value) {
        buf.writeMedium(value != null ? value.getValue().intValue() : 0);
    }
}
