/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.query;

import static com.google.common.base.Verify.verifyNotNull;
import static org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import static org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import static org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.DataInput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

final class PotassiumDataInput {
    // Known singleton objects
    private static final @NonNull Byte INT8_0 = 0;
    private static final @NonNull Short INT16_0 = 0;
    private static final @NonNull Integer INT32_0 = 0;
    private static final @NonNull Long INT64_0 = 0L;
    private static final byte @NonNull[] BINARY_0 = new byte[0];
    private final @NonNull DataInput input;
    private final List<String> codedStrings = new ArrayList<>();

    PotassiumDataInput(@NonNull DataInput input) {
        this.input = input;
    }

    public @NonNull Object readObject() throws IOException {
        final byte type = input.readByte();
        switch (type) {
            case PotassiumValue.BOOLEAN_FALSE:
                return Boolean.FALSE;
            case PotassiumValue.BOOLEAN_TRUE:
                return Boolean.TRUE;
            case PotassiumValue.EMPTY:
                return Empty.value();
            case PotassiumValue.INT8:
                return input.readByte();
            case PotassiumValue.INT8_0:
                return INT8_0;
            case PotassiumValue.INT16:
                return input.readShort();
            case PotassiumValue.INT16_0:
                return INT16_0;
            case PotassiumValue.INT32:
                return input.readInt();
            case PotassiumValue.INT32_0:
                return INT32_0;
            case PotassiumValue.INT32_2B:
                return input.readShort() & 0xFFFF;
            case PotassiumValue.INT64:
                return input.readLong();
            case PotassiumValue.INT64_0:
                return INT64_0;
            case PotassiumValue.INT64_4B:
                return input.readInt() & 0xFFFFFFFFL;
            case PotassiumValue.UINT8:
                return Uint8.fromByteBits(input.readByte());
            case PotassiumValue.UINT8_0:
                return Uint8.ZERO;
            case PotassiumValue.UINT16:
                return Uint16.fromShortBits(input.readShort());
            case PotassiumValue.UINT16_0:
                return Uint16.ZERO;
            case PotassiumValue.UINT32:
                return Uint32.fromIntBits(input.readInt());
            case PotassiumValue.UINT32_0:
                return Uint32.ZERO;
            case PotassiumValue.UINT32_2B:
                return Uint32.fromIntBits(input.readShort() & 0xFFFF);
            case PotassiumValue.UINT64:
                return Uint64.fromLongBits(input.readLong());
            case PotassiumValue.UINT64_0:
                return Uint64.ZERO;
            case PotassiumValue.UINT64_4B:
                return Uint64.fromLongBits(input.readInt() & 0xFFFFFFFFL);
            case PotassiumValue.DECIMAL64:
                return Decimal64.of(input.readByte(), WritableObjects.readLong(input));
            case PotassiumValue.STRING_EMPTY:
                return "";
            case PotassiumValue.STRING_UTF:
                return input.readUTF();
            case PotassiumValue.STRING_2B:
                return readString2();
            case PotassiumValue.STRING_4B:
                return readString4();
            case PotassiumValue.STRING_CHARS:
                return readCharsString();
            case PotassiumValue.BINARY_0:
                return BINARY_0;
            case PotassiumValue.BINARY_1B:
                return readBinary(128 + input.readUnsignedByte());
            case PotassiumValue.BINARY_2B:
                return readBinary(384 + input.readUnsignedShort());
            case PotassiumValue.BINARY_4B:
                return readBinary(input.readInt());
            case PotassiumValue.YIID_0:
                return YangInstanceIdentifier.of();
            case PotassiumValue.YIID:
                return readYangInstanceIdentifier(input.readInt());
            case PotassiumValue.QNAME:
                return readQName();
            case PotassiumValue.BITS_0:
                return ImmutableSet.of();
            case PotassiumValue.BITS_1B:
                return readBits(input.readUnsignedByte() + 29);
            case PotassiumValue.BITS_2B:
                return readBits(input.readUnsignedShort() + 285);
            case PotassiumValue.BITS_4B:
                return readBits(input.readInt());

            default:
                if (type > PotassiumValue.BINARY_0 && type <= PotassiumValue.BINARY_127) {
                    return readBinary(type - PotassiumValue.BINARY_0);
                } else if (type > PotassiumValue.BITS_0 && type < PotassiumValue.BITS_1B) {
                    return readBits(type - PotassiumValue.BITS_0);
                } else if (type > PotassiumValue.YIID_0) {
                    // Note 'byte' is range limited, so it is always '&& type <= PotassiumValue.YIID_31'
                    return readYangInstanceIdentifier(type - PotassiumValue.YIID_0);
                } else {
                    throw new IllegalStateException("Invalid value type " + type);
                }
        }
    }

    private @NonNull String readString2() throws IOException {
        return readByteString(input.readUnsignedShort());
    }

    private @NonNull String readString4() throws IOException {
        return readByteString(input.readInt());
    }

    private @NonNull String readByteString(final int size) throws IOException {
        if (size > 0) {
            final byte[] bytes = new byte[size];
            input.readFully(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        } else if (size == 0) {
            return "";
        } else {
            throw new IllegalStateException("Invalid String bytes length " + size);
        }
    }

    private @NonNull String readCharsString() throws IOException {
        final int size = input.readInt();
        if (size > 0) {
            final char[] chars = new char[size];
            for (int i = 0; i < size; ++i) {
                chars[i] = input.readChar();
            }
            return String.valueOf(chars);
        } else if (size == 0) {
            return "";
        } else {
            throw new IllegalStateException("Invalid String chars length " + size);
        }
    }

    private byte @NonNull [] readBinary(final int size) throws IOException {
        if (size > 0) {
            final byte[] ret = new byte[size];
            input.readFully(ret);
            return ret;
        } else if (size == 0) {
            return BINARY_0;
        } else {
            throw new IllegalStateException("Invalid binary length " + size);
        }
    }

    private @NonNull QName readQName() throws IOException {
        return QName.readFrom(input);
    }

    private @NonNull YangInstanceIdentifier readYangInstanceIdentifier(final int size) throws IOException {
        if (size > 0) {
            final ImmutableList.Builder<PathArgument> builder = ImmutableList.builderWithExpectedSize(size);
            for (int i = 0; i < size; ++i) {
                builder.add(readPathArgument());
            }
            return YangInstanceIdentifier.of(builder.build());
        } else if (size == 0) {
            return YangInstanceIdentifier.of();
        } else {
            throw new IllegalStateException("Invalid YangInstanceIdentifier size " + size);
        }
    }

    public PathArgument readPathArgument() throws IOException {
        final byte header = input.readByte();
        return switch (header & PotassiumPathArgument.TYPE_MASK) {
            case PotassiumPathArgument.NODE_IDENTIFIER -> {
                verifyPathIdentifierOnly(header);
                yield readNodeIdentifier();
            }
            case PotassiumPathArgument.NODE_IDENTIFIER_WITH_PREDICATES -> readNodeIdentifierWithPredicates(header);
            case PotassiumPathArgument.NODE_WITH_VALUE -> {
                verifyPathIdentifierOnly(header);
                yield readNodeWithValue();
            }
            default -> throw new IllegalStateException("Unexpected PathArgument header " + header);
        };
    }

    private NodeIdentifier readNodeIdentifier() throws IOException {
        return NodeIdentifier.create(readQName());
    }

    private NodeIdentifierWithPredicates readNodeIdentifierWithPredicates(final byte header)
            throws IOException {
        final QName qname = readNodeIdentifier().getNodeType();
        return switch (mask(header, PotassiumPathArgument.SIZE_MASK)) {
            case PotassiumPathArgument.SIZE_1B -> readNodeIdentifierWithPredicates(qname, input.readUnsignedByte());
            case PotassiumPathArgument.SIZE_2B -> readNodeIdentifierWithPredicates(qname, input.readUnsignedShort());
            case PotassiumPathArgument.SIZE_4B -> readNodeIdentifierWithPredicates(qname, input.readInt());
            default -> readNodeIdentifierWithPredicates(qname, rshift(header, PotassiumPathArgument.SIZE_SHIFT));
        };
    }

    private NodeIdentifierWithPredicates readNodeIdentifierWithPredicates(final QName qname, final int size)
            throws IOException {
        if (size == 1) {
            return NodeIdentifierWithPredicates.of(qname, readQName(), readObject());
        } else if (size > 1) {
            final ImmutableMap.Builder<QName, Object> builder = ImmutableMap.builderWithExpectedSize(size);
            for (int i = 0; i < size; ++i) {
                builder.put(readQName(), readObject());
            }
            return NodeIdentifierWithPredicates.of(qname, builder.build());
        } else if (size == 0) {
            return NodeIdentifierWithPredicates.of(qname);
        } else {
            throw new IllegalStateException("Invalid predicate count " + size);
        }
    }

    private YangInstanceIdentifier.NodeWithValue<?> readNodeWithValue() throws IOException {
        final QName qname = readNodeIdentifier().getNodeType();
        return new YangInstanceIdentifier.NodeWithValue<>(qname, readObject());
    }

    private @NonNull ImmutableSet<String> readBits(final int size) throws IOException {
        if (size > 0) {
            final ImmutableSet.Builder<String> builder = ImmutableSet.builder();
            for (int i = 0; i < size; ++i) {
                builder.add(readRefString());
            }
            return builder.build();
        } else if (size == 0) {
            return ImmutableSet.of();
        } else {
            throw new IllegalStateException("Invalid bits length " + size);
        }
    }

    private @NonNull String readRefString() throws IOException {
        return readRefString(input.readByte());
    }

    private @NonNull String readRefString(final byte type) throws IOException {
        final String str;
        switch (type) {
            case PotassiumValue.STRING_REF_1B:
                return lookupString(input.readUnsignedByte());
            case PotassiumValue.STRING_REF_2B:
                return lookupString(input.readUnsignedShort() + 256);
            case PotassiumValue.STRING_REF_4B:
                return lookupString(input.readInt());
            case PotassiumValue.STRING_EMPTY:
                return "";
            case PotassiumValue.STRING_2B:
                str = readString2();
                break;
            case PotassiumValue.STRING_4B:
                str = readString4();
                break;
            case PotassiumValue.STRING_CHARS:
                str = readCharsString();
                break;
            case PotassiumValue.STRING_UTF:
                str = input.readUTF();
                break;
            default:
                throw new IllegalStateException("Unexpected String type " + type);
        }

        // TODO: consider interning Strings -- that would help with bits, but otherwise it's probably not worth it
        codedStrings.add(verifyNotNull(str));
        return str;
    }

    private @NonNull String lookupString(final int index) throws IllegalStateException {
        try {
            return codedStrings.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalStateException("Invalid String reference " + index, e);
        }
    }

    private static void verifyPathIdentifierOnly(final byte header) throws IllegalStateException {
        if (mask(header, PotassiumPathArgument.SIZE_MASK) != 0) {
            throw new IllegalStateException("Invalid path argument header " + header);
        }
    }

    private static byte mask(final byte header, final byte mask) {
        return (byte) (header & mask);
    }

    private static int rshift(final byte header, final byte shift) {
        return (header & 0xFF) >>> shift;
    }
}
