/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.json.codec;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont.VlanId;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.ContBuilder;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Id;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactory;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonWriterFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

public class SimpleBindingRuntimeTest extends AbstractBindingRuntimeTest {
    private static BindingNormalizedNodeSerializer BINDING_CODEC_CONTEXT =
            new BindingCodecContext(getRuntimeContext());
    private static final StringWriter WRITER = new StringWriter();
    private static final EffectiveModelContext SCHEMA_CONTEXT = BindingRuntimeHelpers.createEffectiveModel(
            Cont.class);
    private static final JSONCodecFactory JSON_CODEC_FACTORY = JSONCodecFactorySupplier
            .DRAFT_LHOTKA_NETMOD_YANG_JSON_02
            .getShared(SCHEMA_CONTEXT);
    private static final NormalizedNodeStreamWriter JSON_STREAM = JSONNormalizedNodeStreamWriter
            .createExclusiveWriter(JSON_CODEC_FACTORY,
                    JsonWriterFactory.createJsonWriter(WRITER, 2));
    private static final JSONCodec JSON_CODEC = new JSONCodec();

    @Test
    public void testSimpleContainerSerialization() throws IOException {
        final var cont = new ContBuilder().setVlanId(new VlanId(new Id(Uint16.valueOf(30)))).build();
        final var normalizedNode = BINDING_CODEC_CONTEXT
                .toNormalizedNode(InstanceIdentifier.create(Cont.class), cont)
                .getValue();
        assertThat(normalizedNode, instanceOf(ContainerNode.class));
        final NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(JSON_STREAM);
        nodeWriter.write(normalizedNode).close();

        final String serviceOutputString = JSON_CODEC.serialize(InstanceIdentifier.create(Cont.class), cont);
        assertEquals(WRITER.toString(), serviceOutputString);
    }

    @Test
    public void testSimpleContainerDeserialization() throws IOException {
        final var cont = new ContBuilder().setVlanId(new VlanId(new Id(Uint16.valueOf(30)))).build();
        final String serializedContainer = "{\n"
                + "  \"test:cont\": {\n"
                + "    \"vlan-id\": 30\n"
                + "  }\n"
                + "}";

        final DataObject resultDataObject = JSON_CODEC.deserialize(InstanceIdentifier.create(Cont.class),
                serializedContainer);
        assertThat(resultDataObject, instanceOf(Cont.class));

        final Cont result = (Cont) resultDataObject;
        assertEquals(result.getVlanId(),cont.getVlanId());
    }
}
