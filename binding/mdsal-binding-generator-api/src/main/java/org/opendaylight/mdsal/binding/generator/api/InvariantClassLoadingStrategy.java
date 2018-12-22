/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A {@link ClassLoadingStrategy}, which guarantees that {@link #loadClass(String)} does not depend on the caller
 * context. This implies that the set of available classes is constant. This notably means the strategy may not fall
 * back to using the likes of {@link Thread#getContextClassLoader()}.
 *
 * @author Robert Varga
 */
@Beta
public interface InvariantClassLoadingStrategy extends ClassLoadingStrategy, Immutable {

}
