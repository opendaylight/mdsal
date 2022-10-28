package org.opendaylight.mdsal.binding.json.codec;

import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingCodec;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.util.CompositeNodeDataWithSchema;

public class JSONCodec implements BindingCodec<String, DataObject> {

    private BindingIndependentMappingService mappingService;

    public String serialize(DataObject dataObject) {
        CompositeNodeDataWithSchema node = mappingService.toDataDom(dataObject);
    }

    /*public DataObject deserialize(String str) { .....}*/
}
