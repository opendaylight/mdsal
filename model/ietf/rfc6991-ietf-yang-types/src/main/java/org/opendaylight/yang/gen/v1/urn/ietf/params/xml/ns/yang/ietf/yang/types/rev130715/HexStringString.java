/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715;

import com.google.common.annotations.Beta;
import java.util.HexFormat;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.model.ietf.type.util.ByteArrayLike;
import org.opendaylight.yangtools.yang.common.DerivedString;

/**
 * {@link DerivedString} representation of the {@code hex-string} type as defined by ietf-yang-types. This class
 * captures the basic traits, such as comparator and equality and can be freely subclasses. Convenience specializations
 * for {@link One} through {@link Sixteen} bytes are available.
 */
@Beta
@NonNullByDefault
public abstract class HexStringString extends DerivedString<HexStringString> implements ByteArrayLike {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final byte[] EMPTY_BYTES = new byte[0];
    private static final HexStringString EMPTY = new Array(EMPTY_BYTES);
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    public static HexStringString empty() {
        return EMPTY;
    }

    public static HexStringString valueOf(final byte[] bytes) {
        return switch (bytes.length) {
            case 0 -> EMPTY;
            case 1 -> new One(bytes[0]);
            case 2 -> new Two(bytes[0], bytes[1]);
            case 3 -> new Three(bytes[0], bytes[1], bytes[2]);
            case 4 -> new Four(bytes[0], bytes[1], bytes[2], bytes[3]);
            case 5 -> new Five(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4]);
            case 6 -> new Six(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5]);
            case 7 -> new Seven(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6]);
            case 8 -> new Eight(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7]);
            case 9 -> new Nine(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7],
                                bytes[8]);
            case 10 -> new Ten(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7], bytes[8],
                                bytes[9]);
            case 11 -> new Eleven(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7],
                                bytes[8], bytes[9], bytes[10]);
            case 12 -> new Twelve(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7],
                                bytes[8], bytes[9], bytes[10], bytes[11]);
            case 13 -> new Thirteen(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7],
                                bytes[8], bytes[9], bytes[10], bytes[11], bytes[12]);
            case 14 -> new Fourteen(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7],
                                bytes[8], bytes[9], bytes[10], bytes[11], bytes[12], bytes[13]);
            case 15 -> new Fifteen(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7],
                                bytes[8], bytes[9], bytes[10], bytes[11], bytes[12], bytes[13], bytes[14]);
            case 16 -> new Sixteen(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7],
                                bytes[8], bytes[9], bytes[10], bytes[11], bytes[12], bytes[13], bytes[14], bytes[15]);
            default -> new Array(bytes.clone());
        };
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final int compareTo(final HexStringString o) {
        for (int i = 0; i < getLength(); ++i) {
            if (i < o.getLength()) {
                int cmp = Byte.compare(getByteAt(i), o.getByteAt(i));
                if (cmp != 0) {
                    return cmp;
                }
            } else {
                return 1;
            }
        }

        return getLength() - o.getLength();
    }

    @Override
    public final String toCanonicalString() {
        if (getLength() == 0) {
            return "";
        }

        final StringBuilder sb = new StringBuilder(getLength() * 3 - 1);
        HEX_FORMAT.toHexDigits(sb, getByteAt(0));
        for (int i = 1; i < getByteAt(i); ++i) {
            HEX_FORMAT.toHexDigits(sb.append(':'), getByteAt(i));
        }
        return sb.toString();
    }

    @Override
    public final HexStringStringSupport support() {
        return HexStringStringSupport.getInstance();
    }

    @Override
    public final int hashCode() {
        int result = 1;
        for (int i = 0; i < getLength(); ++i) {
            result = 31 * result + getByteAt(i);
        }
        return result;
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof final HexStringString other)) {
            return false;
        }
        if (getLength() != other.getLength()) {
            return false;
        }
        for (int i = 0; i < getLength(); i++) {
            if (getByteAt(i) != other.getByteAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static class Array extends HexStringString {
        private static final long serialVersionUID = 1L;

        private final byte[] bytes;

        protected Array(final byte[] bytes) {
            this.bytes = bytes.length == 0 ? EMPTY_BYTES : bytes;
        }

        @Override
        public final byte getByteAt(final int offset) {
            return bytes[offset];
        }

        @Override
        public final int getLength() {
            return bytes.length;
        }

        @Override
        public final byte[] toByteArray() {
            return bytes.length == 0 ? bytes : bytes.clone();
        }
    }

    public static class One extends HexStringString {
        private static final long serialVersionUID = 1L;

        private final byte byte0;

        protected One(final byte byte0) {
            this.byte0 = byte0;
        }

        protected One(final One other) {
            this(other.byte0);
        }

        @Override
        public final byte getByteAt(final int offset) {
            if (offset == 0) {
                return byte0;
            }
            throw new IndexOutOfBoundsException("Invalid offset " + offset);
        }

        @Override
        public final int getLength() {
            return 1;
        }

        @Override
        public final byte[] toByteArray() {
            return new byte[] { byte0 };
        }
    }

    public static class Two extends HexStringString {
        private static final long serialVersionUID = 1L;

        private final byte byte0;
        private final byte byte1;

        protected Two(final byte byte0, final byte byte1) {
            this.byte0 = byte0;
            this.byte1 = byte1;
        }

        protected Two(final Three other) {
            this(other.byte0, other.byte1);
        }

        @Override
        public final byte getByteAt(final int offset) {
            return switch (offset) {
                case 0 -> byte0;
                case 1 -> byte1;
                default -> throw new IndexOutOfBoundsException("Invalid offset " + offset);
            };
        }

        @Override
        public final int getLength() {
            return 2;
        }

        @Override
        public final byte[] toByteArray() {
            return new byte[] { byte0, byte1 };
        }
    }

    public static class Three extends HexStringString {
        private static final long serialVersionUID = 1L;

        private final byte byte0;
        private final byte byte1;
        private final byte byte2;

        protected Three(final byte byte0, final byte byte1, final byte byte2) {
            this.byte0 = byte0;
            this.byte1 = byte1;
            this.byte2 = byte2;
        }

        protected Three(final Three other) {
            this(other.byte0, other.byte1, other.byte2);
        }

        @Override
        public final byte getByteAt(final int offset) {
            return switch (offset) {
                case 0 -> byte0;
                case 1 -> byte1;
                case 2 -> byte2;
                default -> throw new IndexOutOfBoundsException("Invalid offset " + offset);
            };
        }

        @Override
        public final int getLength() {
            return 3;
        }

        @Override
        public final byte[] toByteArray() {
            return new byte[] { byte0, byte1, byte2 };
        }
    }

    public static class Four extends HexStringString {
        private static final long serialVersionUID = 1L;

        private final byte byte0;
        private final byte byte1;
        private final byte byte2;
        private final byte byte3;

        protected Four(final byte byte0, final byte byte1, final byte byte2, final byte byte3) {
            this.byte0 = byte0;
            this.byte1 = byte1;
            this.byte2 = byte2;
            this.byte3 = byte3;
        }

        protected Four(final Four other) {
            this(other.byte0, other.byte1, other.byte2, other.byte3);
        }

        @Override
        public final byte getByteAt(final int offset) {
            return switch (offset) {
                case 0 -> byte0;
                case 1 -> byte1;
                case 2 -> byte2;
                case 3 -> byte3;
                default -> throw new IndexOutOfBoundsException("Invalid offset " + offset);
            };
        }

        @Override
        public final int getLength() {
            return 4;
        }

        @Override
        public final byte[] toByteArray() {
            return new byte[] { byte0, byte1, byte2, byte3 };
        }
    }

    public static class Five extends HexStringString {
        private static final long serialVersionUID = 1L;

        private final byte byte0;
        private final byte byte1;
        private final byte byte2;
        private final byte byte3;
        private final byte byte4;

        protected Five(final byte byte0, final byte byte1, final byte byte2, final byte byte3, final byte byte4) {
            this.byte0 = byte0;
            this.byte1 = byte1;
            this.byte2 = byte2;
            this.byte3 = byte3;
            this.byte4 = byte4;
        }

        protected Five(final Five other) {
            this(other.byte0, other.byte1, other.byte2, other.byte3, other.byte4);
        }

        @Override
        public final byte getByteAt(final int offset) {
            return switch (offset) {
                case 0 -> byte0;
                case 1 -> byte1;
                case 2 -> byte2;
                case 3 -> byte3;
                case 4 -> byte4;
                default -> throw new IndexOutOfBoundsException("Invalid offset " + offset);
            };
        }

        @Override
        public final int getLength() {
            return 5;
        }

        @Override
        public final byte[] toByteArray() {
            return new byte[] { byte0, byte1, byte2, byte3, byte4 };
        }
    }

    public static class Six extends HexStringString {
        private static final long serialVersionUID = 1L;

        private final byte byte0;
        private final byte byte1;
        private final byte byte2;
        private final byte byte3;
        private final byte byte4;
        private final byte byte5;

        protected Six(final byte byte0, final byte byte1, final byte byte2, final byte byte3, final byte byte4,
                final byte byte5) {
            this.byte0 = byte0;
            this.byte1 = byte1;
            this.byte2 = byte2;
            this.byte3 = byte3;
            this.byte4 = byte4;
            this.byte5 = byte5;
        }

        protected Six(final Six other) {
            this(other.byte0, other.byte1, other.byte2, other.byte3, other.byte4, other.byte5);
        }

        @Override
        public final byte getByteAt(final int offset) {
            return switch (offset) {
                case 0 -> byte0;
                case 1 -> byte1;
                case 2 -> byte2;
                case 3 -> byte3;
                case 4 -> byte4;
                case 5 -> byte5;
                default -> throw new IndexOutOfBoundsException("Invalid offset " + offset);
            };
        }

        @Override
        public final int getLength() {
            return 6;
        }

        @Override
        public final byte[] toByteArray() {
            return new byte[] { byte0, byte1, byte2, byte3, byte4, byte5 };
        }
    }

    public static class Seven extends HexStringString {
        private static final long serialVersionUID = 1L;

        private final byte byte0;
        private final byte byte1;
        private final byte byte2;
        private final byte byte3;
        private final byte byte4;
        private final byte byte5;
        private final byte byte6;

        protected Seven(final byte byte0, final byte byte1, final byte byte2, final byte byte3, final byte byte4,
                final byte byte5, final byte byte6) {
            this.byte0 = byte0;
            this.byte1 = byte1;
            this.byte2 = byte2;
            this.byte3 = byte3;
            this.byte4 = byte4;
            this.byte5 = byte5;
            this.byte6 = byte6;
        }

        protected Seven(final Seven other) {
            this(other.byte0, other.byte1, other.byte2, other.byte3, other.byte4, other.byte5, other.byte6);
        }

        @Override
        public final byte getByteAt(final int offset) {
            return switch (offset) {
                case 0 -> byte0;
                case 1 -> byte1;
                case 2 -> byte2;
                case 3 -> byte3;
                case 4 -> byte4;
                case 5 -> byte5;
                case 6 -> byte6;
                default -> throw new IndexOutOfBoundsException("Invalid offset " + offset);
            };
        }

        @Override
        public final int getLength() {
            return 7;
        }

        @Override
        public final byte[] toByteArray() {
            return new byte[] { byte0, byte1, byte2, byte3, byte4, byte5, byte6 };
        }
    }

    public static class Eight extends HexStringString {
        private static final long serialVersionUID = 1L;

        private final byte byte0;
        private final byte byte1;
        private final byte byte2;
        private final byte byte3;
        private final byte byte4;
        private final byte byte5;
        private final byte byte6;
        private final byte byte7;

        protected Eight(final byte byte0, final byte byte1, final byte byte2, final byte byte3, final byte byte4,
                final byte byte5, final byte byte6, final byte byte7) {
            this.byte0 = byte0;
            this.byte1 = byte1;
            this.byte2 = byte2;
            this.byte3 = byte3;
            this.byte4 = byte4;
            this.byte5 = byte5;
            this.byte6 = byte6;
            this.byte7 = byte7;
        }

        protected Eight(final Eight other) {
            this(other.byte0, other.byte1, other.byte2, other.byte3, other.byte4, other.byte5, other.byte6,
                other.byte7);
        }

        @Override
        public final byte getByteAt(final int offset) {
            return switch (offset) {
                case 0 -> byte0;
                case 1 -> byte1;
                case 2 -> byte2;
                case 3 -> byte3;
                case 4 -> byte4;
                case 5 -> byte5;
                case 6 -> byte6;
                case 7 -> byte7;
                default -> throw new IndexOutOfBoundsException("Invalid offset " + offset);
            };
        }

        @Override
        public final int getLength() {
            return 8;
        }

        @Override
        public final byte[] toByteArray() {
            return new byte[] { byte0, byte1, byte2, byte3, byte4, byte5, byte6, byte7 };
        }
    }

    public static class Nine extends HexStringString {
        private static final long serialVersionUID = 1L;

        private final byte byte0;
        private final byte byte1;
        private final byte byte2;
        private final byte byte3;
        private final byte byte4;
        private final byte byte5;
        private final byte byte6;
        private final byte byte7;
        private final byte byte8;

        protected Nine(final byte byte0, final byte byte1, final byte byte2, final byte byte3, final byte byte4,
                final byte byte5, final byte byte6, final byte byte7, final byte byte8) {
            this.byte0 = byte0;
            this.byte1 = byte1;
            this.byte2 = byte2;
            this.byte3 = byte3;
            this.byte4 = byte4;
            this.byte5 = byte5;
            this.byte6 = byte6;
            this.byte7 = byte7;
            this.byte8 = byte8;
        }

        protected Nine(final Nine other) {
            this(other.byte0, other.byte1, other.byte2, other.byte3, other.byte4, other.byte5, other.byte6,
                other.byte7, other.byte8);
        }

        @Override
        public final byte getByteAt(final int offset) {
            return switch (offset) {
                case 0 -> byte0;
                case 1 -> byte1;
                case 2 -> byte2;
                case 3 -> byte3;
                case 4 -> byte4;
                case 5 -> byte5;
                case 6 -> byte6;
                case 7 -> byte7;
                case 8 -> byte8;
                default -> throw new IndexOutOfBoundsException("Invalid offset " + offset);
            };
        }

        @Override
        public final int getLength() {
            return 9;
        }

        @Override
        public final byte[] toByteArray() {
            return new byte[] { byte0, byte1, byte2, byte3, byte4, byte5, byte6, byte7, byte8 };
        }
    }

    public static class Ten extends HexStringString {
        private static final long serialVersionUID = 1L;

        private final byte byte0;
        private final byte byte1;
        private final byte byte2;
        private final byte byte3;
        private final byte byte4;
        private final byte byte5;
        private final byte byte6;
        private final byte byte7;
        private final byte byte8;
        private final byte byte9;

        protected Ten(final byte byte0, final byte byte1, final byte byte2, final byte byte3, final byte byte4,
                final byte byte5, final byte byte6, final byte byte7, final byte byte8, final byte byte9) {
            this.byte0 = byte0;
            this.byte1 = byte1;
            this.byte2 = byte2;
            this.byte3 = byte3;
            this.byte4 = byte4;
            this.byte5 = byte5;
            this.byte6 = byte6;
            this.byte7 = byte7;
            this.byte8 = byte8;
            this.byte9 = byte9;
        }

        protected Ten(final Ten other) {
            this(other.byte0, other.byte1, other.byte2, other.byte3, other.byte4, other.byte5, other.byte6,
                other.byte7, other.byte8, other.byte9);
        }

        @Override
        public final byte getByteAt(final int offset) {
            return switch (offset) {
                case 0 -> byte0;
                case 1 -> byte1;
                case 2 -> byte2;
                case 3 -> byte3;
                case 4 -> byte4;
                case 5 -> byte5;
                case 6 -> byte6;
                case 7 -> byte7;
                case 8 -> byte8;
                case 9 -> byte9;
                default -> throw new IndexOutOfBoundsException("Invalid offset " + offset);
            };
        }

        @Override
        public final int getLength() {
            return 10;
        }

        @Override
        public final byte[] toByteArray() {
            return new byte[] { byte0, byte1, byte2, byte3, byte4, byte5, byte6, byte7, byte8, byte9 };
        }
    }

    public static class Eleven extends HexStringString {
        private static final long serialVersionUID = 1L;

        private final byte byte0;
        private final byte byte1;
        private final byte byte2;
        private final byte byte3;
        private final byte byte4;
        private final byte byte5;
        private final byte byte6;
        private final byte byte7;
        private final byte byte8;
        private final byte byte9;
        private final byte byte10;

        protected Eleven(final byte byte0, final byte byte1, final byte byte2, final byte byte3, final byte byte4,
                final byte byte5, final byte byte6, final byte byte7, final byte byte8, final byte byte9,
                final byte byte10) {
            this.byte0 = byte0;
            this.byte1 = byte1;
            this.byte2 = byte2;
            this.byte3 = byte3;
            this.byte4 = byte4;
            this.byte5 = byte5;
            this.byte6 = byte6;
            this.byte7 = byte7;
            this.byte8 = byte8;
            this.byte9 = byte9;
            this.byte10 = byte10;
        }

        protected Eleven(final Eleven other) {
            this(other.byte0, other.byte1, other.byte2, other.byte3, other.byte4, other.byte5, other.byte6,
                other.byte7, other.byte8, other.byte9, other.byte10);
        }

        @Override
        public final byte getByteAt(final int offset) {
            return switch (offset) {
                case 0 -> byte0;
                case 1 -> byte1;
                case 2 -> byte2;
                case 3 -> byte3;
                case 4 -> byte4;
                case 5 -> byte5;
                case 6 -> byte6;
                case 7 -> byte7;
                case 8 -> byte8;
                case 9 -> byte9;
                case 10 -> byte10;
                default -> throw new IndexOutOfBoundsException("Invalid offset " + offset);
            };
        }

        @Override
        public final int getLength() {
            return 11;
        }

        @Override
        public final byte[] toByteArray() {
            return new byte[] { byte0, byte1, byte2, byte3, byte4, byte5, byte6, byte7, byte8, byte9, byte10 };
        }
    }

    public static class Twelve extends HexStringString {
        private static final long serialVersionUID = 1L;

        private final byte byte0;
        private final byte byte1;
        private final byte byte2;
        private final byte byte3;
        private final byte byte4;
        private final byte byte5;
        private final byte byte6;
        private final byte byte7;
        private final byte byte8;
        private final byte byte9;
        private final byte byte10;
        private final byte byte11;

        protected Twelve(final byte byte0, final byte byte1, final byte byte2, final byte byte3, final byte byte4,
                final byte byte5, final byte byte6, final byte byte7, final byte byte8, final byte byte9,
                final byte byte10, final byte byte11) {
            this.byte0 = byte0;
            this.byte1 = byte1;
            this.byte2 = byte2;
            this.byte3 = byte3;
            this.byte4 = byte4;
            this.byte5 = byte5;
            this.byte6 = byte6;
            this.byte7 = byte7;
            this.byte8 = byte8;
            this.byte9 = byte9;
            this.byte10 = byte10;
            this.byte11 = byte11;
        }

        protected Twelve(final Twelve other) {
            this(other.byte0, other.byte1, other.byte2, other.byte3, other.byte4, other.byte5, other.byte6,
                other.byte7, other.byte8, other.byte9, other.byte10, other.byte11);
        }

        @Override
        public final byte getByteAt(final int offset) {
            return switch (offset) {
                case 0 -> byte0;
                case 1 -> byte1;
                case 2 -> byte2;
                case 3 -> byte3;
                case 4 -> byte4;
                case 5 -> byte5;
                case 6 -> byte6;
                case 7 -> byte7;
                case 8 -> byte8;
                case 9 -> byte9;
                case 10 -> byte10;
                case 11 -> byte11;
                default -> throw new IndexOutOfBoundsException("Invalid offset " + offset);
            };
        }

        @Override
        public final int getLength() {
            return 12;
        }

        @Override
        public final byte[] toByteArray() {
            return new byte[] { byte0, byte1, byte2, byte3, byte4, byte5, byte6, byte7, byte8, byte9, byte10, byte11 };
        }
    }

    public static class Thirteen extends HexStringString {
        private static final long serialVersionUID = 1L;

        private final byte byte0;
        private final byte byte1;
        private final byte byte2;
        private final byte byte3;
        private final byte byte4;
        private final byte byte5;
        private final byte byte6;
        private final byte byte7;
        private final byte byte8;
        private final byte byte9;
        private final byte byte10;
        private final byte byte11;
        private final byte byte12;

        protected Thirteen(final byte byte0, final byte byte1, final byte byte2, final byte byte3, final byte byte4,
                final byte byte5, final byte byte6, final byte byte7, final byte byte8, final byte byte9,
                final byte byte10, final byte byte11, final byte byte12) {
            this.byte0 = byte0;
            this.byte1 = byte1;
            this.byte2 = byte2;
            this.byte3 = byte3;
            this.byte4 = byte4;
            this.byte5 = byte5;
            this.byte6 = byte6;
            this.byte7 = byte7;
            this.byte8 = byte8;
            this.byte9 = byte9;
            this.byte10 = byte10;
            this.byte11 = byte11;
            this.byte12 = byte12;
        }

        protected Thirteen(final Thirteen other) {
            this(other.byte0, other.byte1, other.byte2, other.byte3, other.byte4, other.byte5, other.byte6,
                other.byte7, other.byte8, other.byte9, other.byte10, other.byte11, other.byte12);
        }

        @Override
        public final byte getByteAt(final int offset) {
            return switch (offset) {
                case 0 -> byte0;
                case 1 -> byte1;
                case 2 -> byte2;
                case 3 -> byte3;
                case 4 -> byte4;
                case 5 -> byte5;
                case 6 -> byte6;
                case 7 -> byte7;
                case 8 -> byte8;
                case 9 -> byte9;
                case 10 -> byte10;
                case 11 -> byte11;
                case 12 -> byte12;
                default -> throw new IndexOutOfBoundsException("Invalid offset " + offset);
            };
        }

        @Override
        public final int getLength() {
            return 13;
        }

        @Override
        public final byte[] toByteArray() {
            return new byte[] { byte0, byte1, byte2, byte3, byte4, byte5, byte6, byte7, byte8, byte9, byte10, byte11,
                byte12 };
        }
    }

    public static class Fourteen extends HexStringString {
        private static final long serialVersionUID = 1L;

        private final byte byte0;
        private final byte byte1;
        private final byte byte2;
        private final byte byte3;
        private final byte byte4;
        private final byte byte5;
        private final byte byte6;
        private final byte byte7;
        private final byte byte8;
        private final byte byte9;
        private final byte byte10;
        private final byte byte11;
        private final byte byte12;
        private final byte byte13;

        protected Fourteen(final byte byte0, final byte byte1, final byte byte2, final byte byte3, final byte byte4,
                final byte byte5, final byte byte6, final byte byte7, final byte byte8, final byte byte9,
                final byte byte10, final byte byte11, final byte byte12, final byte byte13) {
            this.byte0 = byte0;
            this.byte1 = byte1;
            this.byte2 = byte2;
            this.byte3 = byte3;
            this.byte4 = byte4;
            this.byte5 = byte5;
            this.byte6 = byte6;
            this.byte7 = byte7;
            this.byte8 = byte8;
            this.byte9 = byte9;
            this.byte10 = byte10;
            this.byte11 = byte11;
            this.byte12 = byte12;
            this.byte13 = byte13;
        }

        protected Fourteen(final Fourteen other) {
            this(other.byte0, other.byte1, other.byte2, other.byte3, other.byte4, other.byte5, other.byte6,
                other.byte7, other.byte8, other.byte9, other.byte10, other.byte11, other.byte12, other.byte13);
        }

        @Override
        public final byte getByteAt(final int offset) {
            return switch (offset) {
                case 0 -> byte0;
                case 1 -> byte1;
                case 2 -> byte2;
                case 3 -> byte3;
                case 4 -> byte4;
                case 5 -> byte5;
                case 6 -> byte6;
                case 7 -> byte7;
                case 8 -> byte8;
                case 9 -> byte9;
                case 10 -> byte10;
                case 11 -> byte11;
                case 12 -> byte12;
                case 13 -> byte13;
                default -> throw new IndexOutOfBoundsException("Invalid offset " + offset);
            };
        }

        @Override
        public final int getLength() {
            return 14;
        }

        @Override
        public final byte[] toByteArray() {
            return new byte[] { byte0, byte1, byte2, byte3, byte4, byte5, byte6, byte7, byte8, byte9, byte10, byte11,
                byte12, byte13 };
        }
    }

    public static class Fifteen extends HexStringString {
        private static final long serialVersionUID = 1L;

        private final byte byte0;
        private final byte byte1;
        private final byte byte2;
        private final byte byte3;
        private final byte byte4;
        private final byte byte5;
        private final byte byte6;
        private final byte byte7;
        private final byte byte8;
        private final byte byte9;
        private final byte byte10;
        private final byte byte11;
        private final byte byte12;
        private final byte byte13;
        private final byte byte14;

        protected Fifteen(final byte byte0, final byte byte1, final byte byte2, final byte byte3, final byte byte4,
                final byte byte5, final byte byte6, final byte byte7, final byte byte8, final byte byte9,
                final byte byte10, final byte byte11, final byte byte12, final byte byte13, final byte byte14) {
            this.byte0 = byte0;
            this.byte1 = byte1;
            this.byte2 = byte2;
            this.byte3 = byte3;
            this.byte4 = byte4;
            this.byte5 = byte5;
            this.byte6 = byte6;
            this.byte7 = byte7;
            this.byte8 = byte8;
            this.byte9 = byte9;
            this.byte10 = byte10;
            this.byte11 = byte11;
            this.byte12 = byte12;
            this.byte13 = byte13;
            this.byte14 = byte14;
        }

        protected Fifteen(final Fifteen other) {
            this(other.byte0, other.byte1, other.byte2, other.byte3, other.byte4, other.byte5, other.byte6,
                other.byte7, other.byte8, other.byte9, other.byte10, other.byte11, other.byte12, other.byte13,
                other.byte14);
        }

        @Override
        public final byte getByteAt(final int offset) {
            return switch (offset) {
                case 0 -> byte0;
                case 1 -> byte1;
                case 2 -> byte2;
                case 3 -> byte3;
                case 4 -> byte4;
                case 5 -> byte5;
                case 6 -> byte6;
                case 7 -> byte7;
                case 8 -> byte8;
                case 9 -> byte9;
                case 10 -> byte10;
                case 11 -> byte11;
                case 12 -> byte12;
                case 13 -> byte13;
                case 14 -> byte14;
                default -> throw new IndexOutOfBoundsException("Invalid offset " + offset);
            };
        }

        @Override
        public final int getLength() {
            return 15;
        }

        @Override
        public final byte[] toByteArray() {
            return new byte[] { byte0, byte1, byte2, byte3, byte4, byte5, byte6, byte7, byte8, byte9, byte10, byte11,
                byte12, byte13, byte14 };
        }
    }

    public static class Sixteen extends HexStringString {
        private static final long serialVersionUID = 1L;

        private final byte byte0;
        private final byte byte1;
        private final byte byte2;
        private final byte byte3;
        private final byte byte4;
        private final byte byte5;
        private final byte byte6;
        private final byte byte7;
        private final byte byte8;
        private final byte byte9;
        private final byte byte10;
        private final byte byte11;
        private final byte byte12;
        private final byte byte13;
        private final byte byte14;
        private final byte byte15;

        protected Sixteen(final byte byte0, final byte byte1, final byte byte2, final byte byte3, final byte byte4,
                final byte byte5, final byte byte6, final byte byte7, final byte byte8, final byte byte9,
                final byte byte10, final byte byte11, final byte byte12, final byte byte13, final byte byte14,
                final byte byte15) {
            this.byte0 = byte0;
            this.byte1 = byte1;
            this.byte2 = byte2;
            this.byte3 = byte3;
            this.byte4 = byte4;
            this.byte5 = byte5;
            this.byte6 = byte6;
            this.byte7 = byte7;
            this.byte8 = byte8;
            this.byte9 = byte9;
            this.byte10 = byte10;
            this.byte11 = byte11;
            this.byte12 = byte12;
            this.byte13 = byte13;
            this.byte14 = byte14;
            this.byte15 = byte15;
        }

        protected Sixteen(final Sixteen other) {
            this(other.byte0, other.byte1, other.byte2, other.byte3, other.byte4, other.byte5, other.byte6,
                other.byte7, other.byte8, other.byte9, other.byte10, other.byte11, other.byte12, other.byte13,
                other.byte14, other.byte15);
        }

        @Override
        public final byte getByteAt(final int offset) {
            return switch (offset) {
                case 0 -> byte0;
                case 1 -> byte1;
                case 2 -> byte2;
                case 3 -> byte3;
                case 4 -> byte4;
                case 5 -> byte5;
                case 6 -> byte6;
                case 7 -> byte7;
                case 8 -> byte8;
                case 9 -> byte9;
                case 10 -> byte10;
                case 11 -> byte11;
                case 12 -> byte12;
                case 13 -> byte13;
                case 14 -> byte14;
                case 15 -> byte15;
                default -> throw new IndexOutOfBoundsException("Invalid offset " + offset);
            };
        }

        @Override
        public final int getLength() {
            return 16;
        }

        @Override
        public final byte[] toByteArray() {
            return new byte[] { byte0, byte1, byte2, byte3, byte4, byte5, byte6, byte7, byte8, byte9, byte10, byte11,
                byte12, byte13, byte14, byte15 };
        }
    }
}
