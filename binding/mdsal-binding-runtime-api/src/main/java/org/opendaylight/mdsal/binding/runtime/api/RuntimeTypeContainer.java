/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

@Beta
public interface RuntimeTypeContainer extends Immutable {

    @Nullable RuntimeType<?, ?> schemaTreeChild(QName qname);

    @Nullable RuntimeType<?, ?> bindingChild(JavaTypeName typeName);

    // FIXME: consider removing this method
    default @Nullable RuntimeType<?, ?> bindingChild(final Type type) {
        return bindingChild(type.getIdentifier());
    }
}
