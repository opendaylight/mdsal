/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package mdsal.binding.json.codec;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;

public abstract class AbstractBindingRuntimeTest {
    private static BindingRuntimeContext runtimeContext;


    @BeforeClass
    public static void beforeClass() {
        runtimeContext = BindingRuntimeHelpers.createRuntimeContext();
    }

    @AfterClass
    public static void afterClass() {
        runtimeContext = null;
    }

    public static final BindingRuntimeContext getRuntimeContext() {
        return runtimeContext;
    }
}
