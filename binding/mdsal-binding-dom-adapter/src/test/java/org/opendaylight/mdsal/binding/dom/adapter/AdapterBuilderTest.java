/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class AdapterBuilderTest extends AdapterBuilder {

    private static final ClassToInstanceMap<Object> DELEGATES = MutableClassToInstanceMap.create();

    @Test
    public void buildTest() throws Exception {
        this.addDelegate(String.class, "test");
        DELEGATES.putAll((Map) this.build());
        assertTrue(DELEGATES.containsValue("test"));
        this.addDelegate(Object.class, "test2");
        DELEGATES.putAll((Map) this.build());
        assertTrue(DELEGATES.containsValue("test"));
        assertTrue(DELEGATES.get(Object.class).equals("test2"));
    }

    @Override
    public Set<? extends Class> getRequiredDelegates() {
        return DELEGATES.keySet();
    }

    @Override
    protected Object createInstance(ClassToInstanceMap delegates) {
        return delegates;
    }
}