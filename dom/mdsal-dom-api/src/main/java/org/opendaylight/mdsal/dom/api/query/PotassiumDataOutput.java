/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.query;

import static com.google.common.base.Preconditions.checkArgument;
import static org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import static org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import static org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.WritableObjects;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

final class PotassiumDataOutput {
    private final DataOutput output;
    private final Map<String, Integer> stringCodeMap = new HashMap<>();

    PotassiumDataOutput(DataOutput output) {
        this.output = output;
    }

    public void writeObject(final @NonNull Object value) throws IOException {
        if (value instanceof String str) {
            writeValue(str);
        } else if (value instanceof Boolean bool) {
            writeValue(bool);
        } else if (value instanceof Byte byteVal) {
            writeValue(byteVal);
        } else if (value instanceof Short shortVal) {
            writeValue(shortVal);
        } else if (value instanceof Integer intVal) {
            writeValue(intVal);
        } else if (value instanceof Long longVal) {
            writeValue(longVal);
        } else if (value instanceof Uint8 uint8) {
            writeValue(uint8);
        } else if (value instanceof Uint16 uint16) {
            writeValue(uint16);
        } else if (value instanceof Uint32 uint32) {
            writeValue(uint32);
        } else if (value instanceof Uint64 uint64) {
            writeValue(uint64);
        } else if (value instanceof QName qname) {
            writeValue(qname);
        } else if (value instanceof YangInstanceIdentifier id) {
            writeValue(id);
        } else if (value instanceof byte[] bytes) {
            writeValue(bytes);
        } else if (value instanceof Empty) {
            output.writeByte(PotassiumValue.EMPTY);
        } else if (value instanceof Set<?> set) {
            writeValue(set);
        } else if (value instanceof Decimal64 decimal) {
            output.writeByte(PotassiumValue.DECIMAL64);
            output.writeByte(decimal.scale());
            WritableObjects.writeLong(output, decimal.unscaledValue());
        } else {
            throw new IOException("Unhandled value type " + value.getClass());
        }
    }

    private void writeValue(final String value) throws IOException {
        if (value.isEmpty()) {
            output.writeByte(PotassiumValue.STRING_EMPTY);
        } else if (value.length() <= Short.MAX_VALUE / 2) {
            output.writeByte(PotassiumValue.STRING_UTF);
            output.writeUTF(value);
        } else if (value.length() <= 1048576) {
            final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 65536) {
                output.writeByte(PotassiumValue.STRING_2B);
                output.writeShort(bytes.length);
            } else {
                output.writeByte(PotassiumValue.STRING_4B);
                output.writeInt(bytes.length);
            }
            output.write(bytes);
        } else {
            output.writeByte(PotassiumValue.STRING_CHARS);
            output.writeInt(value.length());
            output.writeChars(value);
        }
    }

    private void writeValue(final boolean value) throws IOException {
        output.writeByte(value ? PotassiumValue.BOOLEAN_TRUE : PotassiumValue.BOOLEAN_FALSE);
    }

    private void writeValue(final byte value) throws IOException {
        if (value != 0) {
            output.writeByte(PotassiumValue.INT8);
            output.writeByte(value);
        } else {
            output.writeByte(PotassiumValue.INT8_0);
        }
    }

    private void writeValue(final short value) throws IOException {
        if (value != 0) {
            output.writeByte(PotassiumValue.INT16);
            output.writeShort(value);
        } else {
            output.writeByte(PotassiumValue.INT16_0);
        }
    }

    private void writeValue(final int value) throws IOException {
        if ((value & 0xFFFF0000) != 0) {
            output.writeByte(PotassiumValue.INT32);
            output.writeInt(value);
        } else if (value != 0) {
            output.writeByte(PotassiumValue.INT32_2B);
            output.writeShort(value);
        } else {
            output.writeByte(PotassiumValue.INT32_0);
        }
    }

    private void writeValue(final long value) throws IOException {
        if ((value & 0xFFFFFFFF00000000L) != 0) {
            output.writeByte(PotassiumValue.INT64);
            output.writeLong(value);
        } else if (value != 0) {
            output.writeByte(PotassiumValue.INT64_4B);
            output.writeInt((int) value);
        } else {
            output.writeByte(PotassiumValue.INT64_0);
        }
    }

    private void writeValue(final Uint8 value) throws IOException {
        final byte b = value.byteValue();
        if (b != 0) {
            output.writeByte(PotassiumValue.UINT8);
            output.writeByte(b);
        } else {
            output.writeByte(PotassiumValue.UINT8_0);
        }
    }

    private void writeValue(final Uint16 value) throws IOException {
        final short s = value.shortValue();
        if (s != 0) {
            output.writeByte(PotassiumValue.UINT16);
            output.writeShort(s);
        } else {
            output.writeByte(PotassiumValue.UINT16_0);
        }
    }

    private void writeValue(final Uint32 value) throws IOException {
        final int i = value.intValue();
        if ((i & 0xFFFF0000) != 0) {
            output.writeByte(PotassiumValue.UINT32);
            output.writeInt(i);
        } else if (i != 0) {
            output.writeByte(PotassiumValue.UINT32_2B);
            output.writeShort(i);
        } else {
            output.writeByte(PotassiumValue.UINT32_0);
        }
    }

    private void writeValue(final Uint64 value) throws IOException {
        final long l = value.longValue();
        if ((l & 0xFFFFFFFF00000000L) != 0) {
            output.writeByte(PotassiumValue.UINT64);
            output.writeLong(l);
        } else if (l != 0) {
            output.writeByte(PotassiumValue.UINT64_4B);
            output.writeInt((int) l);
        } else {
            output.writeByte(PotassiumValue.UINT64_0);
        }
    }

    void writeValue(final QName qname) throws IOException {
        output.writeByte(PotassiumValue.QNAME);
        qname.writeTo(output);
    }

    private void writeValue(final YangInstanceIdentifier value) throws IOException {
        final List<PathArgument> args = value.getPathArguments();
        final int size = args.size();
        if (size > 31) {
            output.writeByte(PotassiumValue.YIID);
            output.writeInt(size);
        } else {
            output.writeByte(PotassiumValue.YIID_0 + size);
        }
        for (PathArgument arg : args) {
            writePathArgumentInternal(arg);
        }
    }

    private void writeValue(final byte[] value) throws IOException {
        if (value.length < 128) {
            output.writeByte(PotassiumValue.BINARY_0 + value.length);
        } else if (value.length < 384) {
            output.writeByte(PotassiumValue.BINARY_1B);
            output.writeByte(value.length - 128);
        } else if (value.length < 65920) {
            output.writeByte(PotassiumValue.BINARY_2B);
            output.writeShort(value.length - 384);
        } else {
            output.writeByte(PotassiumValue.BINARY_4B);
            output.writeInt(value.length);
        }
        output.write(value);
    }

    private void writeValue(final Set<?> value) throws IOException {
        final int size = value.size();
        if (size < 29) {
            output.writeByte(PotassiumValue.BITS_0 + size);
        } else if (size < 285) {
            output.writeByte(PotassiumValue.BITS_1B);
            output.writeByte(size - 29);
        } else if (size < 65821) {
            output.writeByte(PotassiumValue.BITS_2B);
            output.writeShort(size - 285);
        } else {
            output.writeByte(PotassiumValue.BITS_4B);
            output.writeInt(size);
        }

        for (Object bit : value) {
            checkArgument(bit instanceof String, "Expected value type to be String but was %s", bit);
            encodeString((String) bit);
        }
    }

    void writePathArgumentInternal(final PathArgument pathArgument) throws IOException {
        if (pathArgument instanceof NodeIdentifier nid) {
            writeNodeIdentifier(nid);
        } else if (pathArgument instanceof NodeIdentifierWithPredicates nip) {
            writeNodeIdentifierWithPredicates(nip);
        } else if (pathArgument instanceof YangInstanceIdentifier.NodeWithValue<?> niv) {
            writeNodeWithValue(niv);
        } else {
            throw new IOException("Unhandled PathArgument " + pathArgument);
        }
    }

    private void writeNodeIdentifier(final NodeIdentifier identifier) throws IOException {
        writePathArgumentQName(identifier.getNodeType(), PotassiumPathArgument.NODE_IDENTIFIER);
    }

    private void writePathArgumentQName(final QName qname, final byte typeHeader) throws IOException {
        // implied '| PotassiumPathArgument.QNAME_DEF'
        output.writeByte(typeHeader);
        qname.writeTo(output);
    }

    private void writeNodeIdentifierWithPredicates(final NodeIdentifierWithPredicates identifier) throws IOException {
        final int size = identifier.size();
        if (size < 13) {
            writePathArgumentQName(identifier.getNodeType(),
                    (byte) (PotassiumPathArgument.NODE_IDENTIFIER_WITH_PREDICATES
                            | size << PotassiumPathArgument.SIZE_SHIFT));
        } else if (size < 256) {
            writePathArgumentQName(identifier.getNodeType(),
                    (byte) (PotassiumPathArgument.NODE_IDENTIFIER_WITH_PREDICATES | PotassiumPathArgument.SIZE_1B));
            output.writeByte(size);
        } else if (size < 65536) {
            writePathArgumentQName(identifier.getNodeType(),
                    (byte) (PotassiumPathArgument.NODE_IDENTIFIER_WITH_PREDICATES | PotassiumPathArgument.SIZE_2B));
            output.writeShort(size);
        } else {
            writePathArgumentQName(identifier.getNodeType(),
                    (byte) (PotassiumPathArgument.NODE_IDENTIFIER_WITH_PREDICATES | PotassiumPathArgument.SIZE_4B));
            output.writeInt(size);
        }

        writePredicates(identifier);
    }

    private void writePredicates(final NodeIdentifierWithPredicates identifier) throws IOException {
        for (Map.Entry<QName, Object> e : identifier.entrySet()) {
            e.getKey().writeTo(output);
            writeObject(e.getValue());
        }
    }

    private void writeNodeWithValue(final YangInstanceIdentifier.NodeWithValue<?> identifier) throws IOException {
        writePathArgumentQName(identifier.getNodeType(), PotassiumPathArgument.NODE_WITH_VALUE);
        writeObject(identifier.getValue());
    }

    private void encodeString(final @NonNull String str) throws IOException {
        final Integer code = stringCodeMap.get(str);
        if (code != null) {
            writeRef(code);
        } else {
            stringCodeMap.put(str, stringCodeMap.size());
            writeValue(str);
        }
    }

    private void writeRef(final int code) throws IOException {
        final int val = code;
        if (val < 256) {
            output.writeByte(PotassiumValue.STRING_REF_1B);
            output.writeByte(val);
        } else if (val < 65792) {
            output.writeByte(PotassiumValue.STRING_REF_2B);
            output.writeShort(val - 256);
        } else {
            output.writeByte(PotassiumValue.STRING_REF_4B);
            output.writeInt(val);
        }
    }

}
