/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.osgi;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * Combination of {@link EffectiveModelContextProvider}, {@link SchemaSourceProvider} and backing ClassLoader
 * information, specific to OSGi multi-classloader environments. Implementations of this interface are expected to be
 * effectively immutable.
 */
@Beta
public interface OSGiEffectiveModel extends Immutable, EffectiveModelContextProvider,
        SchemaSourceProvider<YangTextSchemaSource> {

}
