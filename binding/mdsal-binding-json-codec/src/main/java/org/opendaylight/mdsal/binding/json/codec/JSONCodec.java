/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.json.codec;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactory;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonParserStream;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonWriterFactory;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

public class JSONCodec {

    private static final BindingIndependentMappingService MAPPING_SERVICE = new BindingIndependentMappingService();

    public <T extends DataObject> String serialize(InstanceIdentifier<T> path, T dataObject)
            throws IOException {
        final NormalizedNode normalizedNode = MAPPING_SERVICE.toDataDom(path, dataObject);
        final var targetType = path.getTargetType();

        final StringWriter writer = new StringWriter();
        final EffectiveModelContext schemaContext = BindingRuntimeHelpers.createEffectiveModel(
                targetType);
        final JSONCodecFactory lhotkaCodecFactory = JSONCodecFactorySupplier
                .DRAFT_LHOTKA_NETMOD_YANG_JSON_02
                .getShared(schemaContext);
        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
                lhotkaCodecFactory, JsonWriterFactory.createJsonWriter(writer, 2));
        final NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);
        nodeWriter.write(normalizedNode).close();
        return writer.toString();
    }

    public <T extends DataObject> DataObject deserialize(InstanceIdentifier<T> path, String str)
            throws IOException {
        final var targetType = path.getTargetType();
        final EffectiveModelContext schemaContext = BindingRuntimeHelpers.createEffectiveModel(targetType);
        final JSONCodecFactory lhotkaCodecFactory = JSONCodecFactorySupplier
                .DRAFT_LHOTKA_NETMOD_YANG_JSON_02
                .getShared(schemaContext);
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        try (JsonParserStream handler = JsonParserStream.create(streamWriter, lhotkaCodecFactory)) {
            handler.parse(new JsonReader(new StringReader(str)));
        }

        final NormalizedNode normalizedNode = result.getResult();
        final QName nodeType = normalizedNode.getIdentifier().getNodeType();
        return MAPPING_SERVICE.fromDataDom(nodeType, normalizedNode);
    }
}
