/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.util.FormattingUtils;

public class YangTextTemplateTest {

    @Test(expected = UnsupportedOperationException.class)
    public void privateConstructTest() throws Throwable {
        final Constructor<FormattingUtils> constructor = FormattingUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}