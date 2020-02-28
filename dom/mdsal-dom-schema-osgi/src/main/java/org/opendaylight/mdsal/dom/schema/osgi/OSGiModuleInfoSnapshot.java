/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.osgi;

import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedLong;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.binding.runtime.api.ModuleInfoSnapshot;

/**
 * Combination of a {@link ModuleInfoSnapshot} with a linear generation.
 */
@Beta
public interface OSGiModuleInfoSnapshot extends ModuleInfoSnapshot {

    @NonNull UnsignedLong getGeneration();
}
