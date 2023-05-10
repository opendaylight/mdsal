/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;

import java.util.ServiceLoader;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.ActionSpec;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecFactory;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Cont;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Grpcont;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Grplst;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Lstio;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Nestedcont;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Othercont;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.Foo;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grpcont.Bar;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.lstio.Fooio;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.nested.Baz;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

public class ActionLookupTest {
    @Test
    public void testActionPath() {
        CurrentAdapterSerializer codec = new CurrentAdapterSerializer(ServiceLoader.load(BindingDOMCodecFactory.class)
                .findFirst().orElseThrow().createBindingDOMCodec(BindingRuntimeHelpers.createRuntimeContext()));

        assertEquals(Absolute.of(Cont.QNAME, Foo.QNAME), codec.getActionPath(
            ActionSpec.builder(Cont.class).build(Foo.class)));
        assertEquals(Absolute.of(Grpcont.QNAME, Bar.QNAME), codec.getActionPath(
            ActionSpec.builder(Grpcont.class).build(Bar.class)));
        assertEquals(Absolute.of(Othercont.QNAME, Bar.QNAME), codec.getActionPath(
            ActionSpec.builder(Othercont.class).build(
                org.opendaylight.yang.gen.v1.urn.odl.actions.norev.othercont.Bar.class)));
        assertEquals(Absolute.of(Nestedcont.QNAME, Baz.QNAME, Bar.QNAME), codec.getActionPath(
            ActionSpec.builder(Nestedcont.class).withPathChild(Baz.class).build(
                org.opendaylight.yang.gen.v1.urn.odl.actions.norev.nested.baz.Bar.class)));
        assertEquals(Absolute.of(Lstio.QNAME, Fooio.QNAME), codec.getActionPath(
            ActionSpec.builder(Lstio.class).build(Fooio.class)));
        assertEquals(Absolute.of(Grplst.QNAME, Bar.QNAME), codec.getActionPath(
            ActionSpec.builder(Grplst.class).build(
                org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grplst.Bar.class)));
    }
}
