/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.model.api.type.builder;

import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;

/**
 * Generated Type Builder interface is helper interface for building and
 * defining the GeneratedType.
 *
 * @see GeneratedType
 *
 * @deprecated Use {@link org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder} instead.
 */
@Deprecated
public interface GeneratedTypeBuilder extends GeneratedTypeBuilderBase<GeneratedTypeBuilder> {

    /**
     * Returns the <code>new</code> <i>immutable</i> instance of Generated Type.
     *
     * @return the <code>new</code> <i>immutable</i> instance of Generated Type.
     */
    GeneratedType toInstance();

}
