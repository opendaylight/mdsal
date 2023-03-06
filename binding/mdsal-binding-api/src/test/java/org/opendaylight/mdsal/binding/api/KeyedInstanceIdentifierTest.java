/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.binding.Key;
import org.opendaylight.yangtools.yang.binding.KeyAware;

@ExtendWith(MockitoExtension.class)
class KeyedInstanceIdentifierTest {
    @Mock
    private Key<?> key;

    @Test
    void basicTest() {
        final var keyedInstanceIdentifier = new KeyedInstanceIdentifier(KeyAware.class, ImmutableList.of(), 0, key);

        assertEquals(key, keyedInstanceIdentifier.getKey());
        assertFalse(keyedInstanceIdentifier.fastNonEqual(keyedInstanceIdentifier.builder().build()));
        assertTrue(new KeyedInstanceIdentifier(Identifiable.class, ImmutableList.of(), 0, null)
                .fastNonEqual(keyedInstanceIdentifier.builder().build()));
    }
}