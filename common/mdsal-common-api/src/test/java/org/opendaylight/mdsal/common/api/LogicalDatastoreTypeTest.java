/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class LogicalDatastoreTypeTest {
    @ParameterizedTest
    @MethodSource
    void serialization(final LogicalDatastoreType type, final byte[] expected) throws Exception {
        final var bout = new ByteArrayOutputStream();
        try (var out = new DataOutputStream(bout)) {
            type.writeTo(out);
        }
        assertArrayEquals(expected, bout.toByteArray());

        try (var in = new DataInputStream(new ByteArrayInputStream(expected))) {
            assertSame(type, LogicalDatastoreType.readFrom(in));
        }
    }

    static Stream<Object[]> serialization() {
        return Stream.of(
            new Object[] { LogicalDatastoreType.OPERATIONAL, new byte[] { 1 }},
            new Object[] { LogicalDatastoreType.CONFIGURATION, new byte[] { 2 }});
    }

    @Test
    void invalidSerialization() throws Exception {
        try (var in = new DataInputStream(new ByteArrayInputStream(new byte[] { 0 }))) {
            final var ex = assertThrows(IOException.class, () -> LogicalDatastoreType.readFrom(in));
            assertEquals("Unknown type 0", ex.getMessage());
        }
    }

}