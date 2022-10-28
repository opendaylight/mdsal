/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.json.codec;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
public class JSONCodec{
    private static BindingIndependentMappingService mappingService = new BindingIndependentMappingService();
    public String serialize(DataObject dataObject) {
        NormalizedNode node = mappingService.toDataDom(dataObject);
        return null;
    }
    public DataObject deserialize(String str) {
        return null;
    }
}