/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.binding.model.api.JavaConstruct.Class;
import org.opendaylight.mdsal.binding.model.api.JavaConstruct.Enum;
import org.opendaylight.mdsal.binding.model.api.JavaConstruct.Interface;
import org.opendaylight.mdsal.binding.model.api.JavaConstruct.Record;

class JavaContructTest {
    @Test
    void testInterface() {
        assertNotNull(Interface.builder().build());
    }

    @Test
    void testClass() {
        assertNotNull(Class.builder().build());
    }

    @Test
    void testEnum() {
        assertNotNull(Enum.builder().build());
    }

    @Test
    void testRecord() {
        assertNotNull(Record.builder().build());
    }
}
