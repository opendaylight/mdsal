package org.opendaylight.mdsal.binding.json.codec;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingCodec;
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

public class JSONCodec implements BindingCodec<String, DataObject> {

    private static BindingIndependentMappingService mappingService = new BindingIndependentMappingService();
    private static final JSONNormalizedNodeStreamWriter jsonNormalizedNodeStreamWriter =
            (JSONNormalizedNodeStreamWriter) JSONNormalizedNodeStreamWriter
                    .createExclusiveWriter(JSONCodecFactorySupplier.RFC7951.createLazy(SimpleSchemaContext.forModules(null)),
                SchemaPath.create(false, QName.create("urn:opendaylight:params:xml:ns")), null, null);

    public String serialize(DataObject dataObject) {
        NormalizedNode node = mappingService.toDataDom(dataObject);

    }

    public DataObject deserialize(String str) {
        return null;
    }
}
