/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Map.Entry;
import javax.xml.transform.dom.DOMSource;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.mdsal437.norev.Cont;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.binding.AbstractOpaqueData;
import org.opendaylight.yangtools.yang.binding.AbstractOpaqueObject;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.OpaqueData;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AnyxmlLeafTest extends AbstractBindingCodecTest {
    private static final NodeIdentifier CONT_NODE_ID = new NodeIdentifier(Cont.QNAME);

    private DOMSource domSource;
    private ContainerNode cont;

    @Override
    public void before() {
        super.before();

        final Document doc = UntrustedXML.newDocumentBuilder().newDocument();
        final Element element = doc.createElement("foo");
        domSource = new DOMSource(element);

        cont = Builders.containerBuilder()
                .withNodeIdentifier(CONT_NODE_ID)
                .withChild(Builders.anyXmlBuilder()
                    .withNodeIdentifier(new NodeIdentifier(org.opendaylight.yang.gen.v1.mdsal437.norev.cont.Cont.QNAME))
                    .withValue(domSource)
                    .build())
                .build();
    }

    @Test
    public void testAnyxmlToBinding() {
        final Entry<InstanceIdentifier<?>, DataObject> readed = registry.fromNormalizedNode(
            YangInstanceIdentifier.create(CONT_NODE_ID), cont);
        assertEquals(InstanceIdentifier.create(Cont.class), readed.getKey());
        final DataObject ldo = readed.getValue();
        assertThat(ldo, instanceOf(Cont.class));

        // So no... Grp should be null ..
        final Cont cont = (Cont) ldo;
        assertNull(cont.getGrp());

        // Cont is interesting
        final org.opendaylight.yang.gen.v1.mdsal437.norev.cont.Cont anyCont = cont.getCont();
        assertNotNull(anyCont);
        assertEquals(org.opendaylight.yang.gen.v1.mdsal437.norev.cont.Cont.class, anyCont.implementedInterface());
        assertEquals(anyCont, anyCont);
        assertNotEquals(anyCont, new TestCont());
        assertEquals(new TestCont(), anyCont);

        final OpaqueData<?> value = anyCont.getValue();
        assertNotNull(value);
        assertEquals(DOMSource.class, value.getObjectModel());
    }

    private final class TestCont
        extends AbstractOpaqueObject<org.opendaylight.yang.gen.v1.mdsal437.norev.cont.Cont>
        implements org.opendaylight.yang.gen.v1.mdsal437.norev.cont.Cont {

        @Override
        public OpaqueData<?> getValue() {
            // TODO Auto-generated method stub
            return new AbstractOpaqueData<NormalizedNode>() {

                @Override
                public Class<NormalizedNode> getObjectModel() {
                    // TODO Auto-generated method stub
                    return NormalizedNode.class;
                }

                @Override
                public NormalizedNode getData() {
                    return cont;
                }
            };
        }
    }
}
