package org.opendaylight.mdsal.binding.json.codec;

import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.impl.DataObjectSerializer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class BindingIndependentMappingService {

    private static final BindingNormalizedNodeCodecRegistry =
        new BindingNormalizedNodeCodecRegistry(DataObjectSerializerGenerator.create(JavassistUtils.forClassPool(new ClassPool)));

    public NormalizedNode toDataDom(DataObject dataObject){

    }

    public DataObject toDataObject(NormalizedNode normalizedNode){
        return null;
    }
}
