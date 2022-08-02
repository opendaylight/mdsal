/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

public final class BitsTypeObjectHelpers {

    private static final int ALL_SET_BITS = 0xFFFFFFFF;

    private static final int WORD_SIZE = 32;

    public static int setBits(int bits, boolean... bitValues) {
        for (int i = 0; i < bitValues.length; i++) {
            bits = setBit(bits, bitValues[i], i);
        }
        return bits;
    }

    public static long setBits(long bits, boolean... bitValues) {
        for (int i = 0; i < bitValues.length; i++) {
            bits = setBit(bits, bitValues[i], i);
        }
        return bits;
    }

    public static int[] setBits(int[] bits, boolean... bitValues) {
        for (int i = 0; i < bitValues.length; i++) {
            bits = setBit(bits, bitValues[i], i);
        }
        return bits;
    }

    public static int setBit(int bits, boolean value, int position) {
        validatePosition(position);
        return value ? bits | 1 << position : bits & ~(1 << position);
    }

    public static long setBit(long bits, boolean value, int position) {
        validatePosition(position);
        return value ? bits | 1L << position : bits & ~(1L << position);
    }

    public static int[] setBit(int[] bits, boolean value, int position) {
        validatePosition(position);
        final int word = bits[position / WORD_SIZE];
        final int shifted = 1 << (position % WORD_SIZE);
        bits[position / WORD_SIZE] = value ? word | shifted : word & (ALL_SET_BITS - shifted);
        return bits;
    }

    public static boolean getBit(int bits, int position) {
        return ((bits >> position) & 1) != 0;
    }

    public static boolean getBit(long bits, int position) {
        return ((bits >> position) & 1) != 0;
    }

    public static boolean getBit(int[] bits, int pos) {
        return (bits[pos / WORD_SIZE] & (1 << (pos % WORD_SIZE))) != 0;
    }

    private static void validatePosition(int position) {
        if (position < 0) {
            throw new IndexOutOfBoundsException("Position < 0: " + position);
        }
    }

    public static int wordSize() {
        return WORD_SIZE;
    }

    private BitsTypeObjectHelpers() {
    }
}
