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
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont.VlanId.Enumeration;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.ContBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactory;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonWriterFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingJSONCodecTest extends AbstractBindingRuntimeTest{

    private static final Logger LOG = LoggerFactory.getLogger(BindingJSONCodecTest.class);

    private static BindingNormalizedNodeSerializer bindingCodecContext = new BindingCodecContext(getRuntimeContext());
    private static final StringWriter writer = new StringWriter();

    private static final EffectiveModelContext schemaContext = BindingRuntimeHelpers.createEffectiveModel(
            Cont.class);
    private static final JSONCodecFactory lhotkaCodecFactory = JSONCodecFactorySupplier
            .DRAFT_LHOTKA_NETMOD_YANG_JSON_02
            .getShared(schemaContext);

    final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
            lhotkaCodecFactory, JsonWriterFactory.createJsonWriter(writer, 2));

    @Test
    public void testSimpleContainer() throws IOException {
        final var cont = new ContBuilder().setVlanId(new Cont.VlanId(Enumeration.forValue(30))).build();
        final var normalizedNode = bindingCodecContext.toNormalizedNode(InstanceIdentifier.create(Cont.class), cont).getValue();
        final NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);
        nodeWriter.write(normalizedNode).close();
        LOG.debug("Serialized JSON: {}", writer);
    }
}

