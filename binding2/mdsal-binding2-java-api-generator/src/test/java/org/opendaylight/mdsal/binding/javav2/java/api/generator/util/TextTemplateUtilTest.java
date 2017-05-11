/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.java.api.generator.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;

public class TextTemplateUtilTest {

    @Test
    public void voidReturnMethodTest() {
        final MethodSignature methodSignature = mock(MethodSignature.class);
        final Type returnType = Types.VOID;
        when(methodSignature.getReturnType()).thenReturn(returnType);
        final String javaDocForInterface = TextTemplateUtil.getJavaDocForInterface(methodSignature);
        Assert.assertEquals("", javaDocForInterface);
    }
}
