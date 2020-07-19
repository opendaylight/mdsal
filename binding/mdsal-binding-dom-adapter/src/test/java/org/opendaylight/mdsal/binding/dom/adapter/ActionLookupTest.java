/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Cont;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Grpcont;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Othercont;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.Foo;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grpcont.Bar;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class ActionLookupTest {
    @Test
    public void testActionSchemaPath() {
        CurrentAdapterSerializer codec = new CurrentAdapterSerializer(new BindingCodecContext(
            BindingRuntimeHelpers.createRuntimeContext()));

        assertEquals(SchemaPath.create(true, Cont.QNAME, Foo.QNAME), codec.getActionPath(Foo.class));
        assertEquals(SchemaPath.create(true, Grpcont.QNAME, Bar.QNAME), codec.getActionPath(Bar.class));
        assertEquals(SchemaPath.create(true, Othercont.QNAME, Bar.QNAME),
            codec.getActionPath(org.opendaylight.yang.gen.v1.urn.odl.actions.norev.othercont.Bar.class));
    }
}
