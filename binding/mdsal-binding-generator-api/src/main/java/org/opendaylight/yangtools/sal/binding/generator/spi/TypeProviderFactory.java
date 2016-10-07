/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.spi;

import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;

/**
 * @deprecated Use {@link org.opendaylight.mdsal.binding.generator.spi.TypeProviderFactory} instead.
 */
@Deprecated
//FIXME not implemented anywhere
public interface TypeProviderFactory {

    TypeProvider providerFor(ModuleIdentifier module);
}
