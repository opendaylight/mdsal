/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.config;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataRoot;

/**
 * Consumer-side of configuration infrastructure. It allows registration to configuration object updates.
 */
@Beta
public interface ConfigurationService {

    <M extends @NonNull DataRoot> ImplementedModule.@NonNull Builder<M> implementModule(Class<M> module);
}
