/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import java.lang.invoke.MethodType;
import org.opendaylight.mdsal.binding.runtime.api.YangDataRuntimeType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.YangData;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;

/**
 * Analysis of a {@link YangData} specialization class.
 */
final class CodecYangDataAnalysis extends AbstractDataContainerAnalysis<YangDataRuntimeType> {
    private static final MethodType CONSTRUCTOR_TYPE = MethodType.methodType(void.class,
        AbstractDataObjectCodecContext.class, DataContainerNode.class);
    private static final MethodType DATAOBJECT_TYPE = MethodType.methodType(DataObject.class,
        AbstractDataObjectCodecContext.class, DataContainerNode.class);

    CodecYangDataAnalysis(final Class<?> bindingClass, final YangDataRuntimeType runtimeType,
            final CodecContextFactory factory, final CodecItemFactory itemFactory) {
        super(bindingClass, runtimeType, factory, itemFactory);
    }
}
