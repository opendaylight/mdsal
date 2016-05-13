/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.model.api.type.builder;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding2.model.api.GeneratedType;

/**
 * Generated Type Builder interface is helper interface for building and
 * defining the GeneratedType.
 *
 * @see GeneratedType
 */
@Beta
public interface GeneratedTypeBuilder extends GeneratedTypeBuilderBase<GeneratedTypeBuilder> {

    /**
     * Returns the <code>new</code> <i>immutable</i> instance of Generated Type.
     *
     * @return the <code>new</code> <i>immutable</i> instance of Generated Type.
     */
    GeneratedType toInstance();

}
