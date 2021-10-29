/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.spi;

import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeTypeContainer;
import org.opendaylight.yangtools.yang.common.QName;

abstract class AbstractCompositeRuntimeType extends AbstractRuntimeType implements RuntimeTypeContainer {
    @Override
    public RuntimeType schemaTreeChild(final QName qname) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public RuntimeType bindingChild(final JavaTypeName typeName) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }
}
