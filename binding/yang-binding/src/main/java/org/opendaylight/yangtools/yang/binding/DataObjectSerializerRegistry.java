/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

/**
 * SPI-level contract for registry of {@link DataObjectSerializer}. The contract is kept between implementation
 * of {@link DataObjectSerializerImplementation}, Registry provides lookup for serializers to support recursive
 * serialization of nested {@link DataObject}s.
 *
 * @deprecated This interface is deprecated because it is an implementation leak.
 */
@Deprecated
public interface DataObjectSerializerRegistry {

    DataObjectSerializer getSerializer(Class<? extends DataObject> binding);

}
