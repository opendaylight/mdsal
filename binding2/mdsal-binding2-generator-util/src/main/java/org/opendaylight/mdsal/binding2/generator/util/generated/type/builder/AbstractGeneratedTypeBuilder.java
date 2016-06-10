/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.util.generated.type.builder;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding2.generator.util.AbstractBaseType;
import org.opendaylight.mdsal.binding2.model.api.type.builder.GeneratedTypeBuilderBase;

@Beta
abstract class AbstractGeneratedTypeBuilder<T extends GeneratedTypeBuilderBase<T>> extends AbstractBaseType
        implements GeneratedTypeBuilderBase<T> {

    protected AbstractGeneratedTypeBuilder(final String packageName, final String name) {
        super(packageName, name);
    }

    protected abstract T thisInstance();

    //TODO: implement methods
}
