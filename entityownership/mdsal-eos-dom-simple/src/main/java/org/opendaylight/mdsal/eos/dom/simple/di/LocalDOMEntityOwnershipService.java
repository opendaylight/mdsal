/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.dom.simple.di;

import com.google.common.annotations.Beta;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.mdsal.eos.dom.simple.SimpleDOMEntityOwnershipService;

/**
 * Simple {@link DOMEntityOwnershipService} operating as an isolated island. It has no awareness of the world outside
 * of itself.
 *
 * @author Robert Varga
 */
@Beta
@Singleton
public class LocalDOMEntityOwnershipService extends SimpleDOMEntityOwnershipService {
    @Inject
    public LocalDOMEntityOwnershipService() {
        // Exposed for DI
    }
}
