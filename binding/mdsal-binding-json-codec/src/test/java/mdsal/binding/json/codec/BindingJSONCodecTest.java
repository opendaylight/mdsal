/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package mdsal.binding.json.codec;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont.VlanId.Enumeration;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.ContBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonWriterFactory;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class BindingJSONCodecTest extends AbstractBindingRuntimeTest{

    private static BindingCodecContext bindingCodecContext = new BindingCodecContext(getRuntimeContext());
    final static Writer writer = new StringWriter();
    final static EffectiveModelContext schemaContext = YangParserTestUtils.parseYangResourceDirectory("/testyangs");
    private static NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.createSimple(schemaContext),
            JsonWriterFactory.createJsonWriter(writer, 2)
    );
    private static NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);

    @Test
    public void testSimpleContainer() throws IOException {
        final var cont = new ContBuilder().setVlanId(new Cont.VlanId(Enumeration.forValue(30))).build();
        NormalizedNode normalizedNode = bindingCodecContext.toNormalizedNode(
                InstanceIdentifier.create(Cont.class),cont).getValue();
        nodeWriter.write(normalizedNode);
        String jsonString = writer.toString();
    }

}
