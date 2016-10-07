/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.model.api.type.builder;

import org.opendaylight.yangtools.sal.binding.model.api.Constant;
import org.opendaylight.yangtools.sal.binding.model.api.Type;

/**
 *
 * @deprecated Use {@link org.opendaylight.mdsal.binding.model.api.type.builder.ConstantBuilder} instead.
 */
@Deprecated
public interface ConstantBuilder {

    void assignValue(final Object value);

    Constant toInstance(Type definingType);
}
