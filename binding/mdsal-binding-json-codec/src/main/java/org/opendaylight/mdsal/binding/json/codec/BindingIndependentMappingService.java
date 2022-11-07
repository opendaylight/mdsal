/*
Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.

This program and the accompanying materials are made available under the
terms of the Eclipse Public License v1.0 which accompanies this distribution,
and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.json.codec;

import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class BindingIndependentMappingService {
    private static final BindingRuntimeContext RUNTIME_CONTEXT = BindingRuntimeHelpers.createRuntimeContext();
    private static final BindingNormalizedNodeSerializer BINDING_CODEC_CONTEXT = new BindingCodecContext(
            RUNTIME_CONTEXT);

    public <T extends DataObject> NormalizedNode toDataDom(final InstanceIdentifier<T> path, final T data) {
        return BINDING_CODEC_CONTEXT.toNormalizedNode(path, data).getValue();
    }

    public DataObject fromDataDom(final QName nodeType, final NormalizedNode node) {
        return BINDING_CODEC_CONTEXT.fromNormalizedNode(YangInstanceIdentifier.builder().node(nodeType).build(), node)
                .getValue();
    }

}