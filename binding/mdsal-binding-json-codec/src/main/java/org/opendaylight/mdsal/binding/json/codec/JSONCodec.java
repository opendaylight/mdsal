/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.json.codec;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.util.CompositeNodeDataWithSchema;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.spi.SimpleSchemaContext;

public class JSONCodec{
    private static BindingIndependentMappingService mappingService = new BindingIndependentMappingService();
    private static final JSONNormalizedNodeStreamWriter jsonNormalizedNodeStreamWriter =
            (JSONNormalizedNodeStreamWriter) JSONNormalizedNodeStreamWriter
                    .createExclusiveWriter(JSONCodecFactorySupplier.RFC7951.createLazy(SimpleSchemaContext.forModules(null)),
                SchemaPath.create(false, QName.create("urn:opendaylight:params:xml:ns")), null, null);

    public String serialize(DataObject dataObject) {
        NormalizedNode node = mappingService.toDataDom(dataObject);
        return null;
    }

    public DataObject deserialize(String str) {
        return null;
    }
}
