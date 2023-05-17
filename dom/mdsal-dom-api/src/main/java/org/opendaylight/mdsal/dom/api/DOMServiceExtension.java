/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.ObjectExtension;

/**
 * Marker interface for services which expose additional functionality on top of some base {@link DOMService}.
 */
@Beta
public interface DOMServiceExtension<T extends DOMExtensibleService<T, E>, E extends DOMServiceExtension<T, E>>
        extends ObjectExtension<T, E> {

}
