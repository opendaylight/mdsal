package org.opendaylight.mdsal.binding.json.codec;

import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingCodec;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.util.CompositeNodeDataWithSchema;

public class JSONCodec implements BindingCodec<String, DataObject> {

    private static BindingIndependentMappingService mappingService = new BindingIndependentMappingService();
    private static NormalizedNodeWriter normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(new NormalizedNodeStreamWriter());

    public String serialize(DataObject dataObject) {
        NormalizedNode node = mappingService.toDataDom(dataObject);
        
    }

    public DataObject deserialize(String str) {
        return null;
    }
}
