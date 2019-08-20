/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.io.IOException;

/**
 * SPI-level contract for implementations of {@link DataObjectSerializer}.
 * The contract is kept between implementation of {@link DataObjectSerializerRegistry},
 * which maintains the lookup context required for recursive serialization.
 *
 * @deprecated This interface is deprecated because it is an implementation leak.
 */
@Deprecated
public interface DataObjectSerializerImplementation {
    /**
     * Writes stream events for supplied data object to provided stream.
     *
     * <p>
     * DataObjectSerializerRegistry may be used to lookup serializers for other generated classes  in order to support
     * writing their events.
     */
    void serialize(DataObjectSerializerRegistry reg, DataObject obj, BindingStreamEventWriter stream)
            throws IOException;
}
