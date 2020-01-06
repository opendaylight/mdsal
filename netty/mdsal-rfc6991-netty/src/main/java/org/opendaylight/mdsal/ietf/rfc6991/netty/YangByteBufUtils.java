/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.ietf.rfc6991.netty;

import com.google.common.annotations.Beta;
import io.netty.buffer.ByteBuf;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;

@Beta
@NonNullByDefault
public final class YangByteBufUtils {
    private YangByteBufUtils() {
        // Hidden on purpose
    }

    public static DottedQuad readDottedQuad(final ByteBuf buf) {
        return IetfYangUtil.INSTANCE.dottedQuadFor(buf.readInt());
    }

    public static void writeDottedQuad(final ByteBuf buf, final DottedQuad quad) {
        buf.writeInt(IetfYangUtil.INSTANCE.dottedQuadBits(quad));
    }
}
