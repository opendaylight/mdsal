/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.opendaylight.yangtools.yang.common.YangConstants.operationInputQName;
import static org.opendaylight.yangtools.yang.common.YangConstants.operationOutputQName;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.containerBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.leafBuilder;

import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Cont;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.Lstio;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.Foo;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.FooInput;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.FooInputBuilder;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.FooOutput;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.FooOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.lstio.Fooio;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.lstio.FooioInput;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.lstio.FooioInputBuilder;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.lstio.FooioOutput;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.lstio.FooioOutputBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

public abstract class AbstractActionAdapterTest extends AbstractAdapterTest  {
    protected static final Absolute FOO_PATH = Absolute.of(Cont.QNAME, Foo.QNAME);
    protected static final Absolute FOOIO_PATH = Absolute.of(Lstio.QNAME, Fooio.QNAME);
    protected static final NodeIdentifier FOO_INPUT = new NodeIdentifier(operationInputQName(Foo.QNAME.getModule()));
    protected static final NodeIdentifier FOO_OUTPUT = new NodeIdentifier(operationOutputQName(Foo.QNAME.getModule()));
    protected static final NodeIdentifier FOO_XYZZY = new NodeIdentifier(QName.create(Foo.QNAME, "xyzzy"));
    protected static final ContainerNode DOM_FOO_INPUT = containerBuilder().withNodeIdentifier(FOO_INPUT)
            .withChild(leafBuilder().withNodeIdentifier(FOO_XYZZY).withValue("xyzzy").build())
            .build();
    protected static final ContainerNode DOM_FOO_OUTPUT = containerBuilder().withNodeIdentifier(FOO_OUTPUT).build();
    protected static final FooInput BINDING_FOO_INPUT = new FooInputBuilder().setXyzzy("xyzzy").build();
    protected static final FooOutput BINDING_FOO_OUTPUT = new FooOutputBuilder().build();
    protected static final FooioInput BINDING_LSTIO_INPUT = new FooioInputBuilder().build();
    protected static final FooioOutput BINDING_LSTIO_OUTPUT = new FooioOutputBuilder().build();

}
