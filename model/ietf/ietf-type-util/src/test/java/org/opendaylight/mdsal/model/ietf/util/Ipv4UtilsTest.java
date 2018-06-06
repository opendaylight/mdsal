/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.model.ietf.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import org.junit.Test;

public class Ipv4UtilsTest {

    @Test(expected = UnsupportedOperationException.class)
    public void privateConstructTest() throws Throwable {
        final Constructor<Ipv4Utils> constructor = Ipv4Utils.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            fail("Exception should be thrown");
        } catch (Exception e) {
            throw e.getCause();
        }
    }
}