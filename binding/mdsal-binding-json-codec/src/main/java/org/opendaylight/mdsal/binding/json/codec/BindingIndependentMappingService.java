/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.json.codec;

import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.impl.DataObjectSerializer;
import org.opendaylight.yangtools.yang.binding.BindingObject;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class BindingIndependentMappingService {

    BindingNormalizedNodeCodec bindingNormalizedNodeCodec = new BindingNormalizedNodeCodec() {
        @Override
        public BindingObject deserialize(@org.eclipse.jdt.annotation.NonNull NormalizedNode data) {
            return null;
        }

        @Override
        public @org.eclipse.jdt.annotation.NonNull NormalizedNode serialize(BindingObject data) {
            return null;
        }
    };

    public NormalizedNode toDataDom(DataObject dataObject) {
        return null;
    }

    public DataObject toDataObject(NormalizedNode normalizedNode){
        return null;
    }
}
