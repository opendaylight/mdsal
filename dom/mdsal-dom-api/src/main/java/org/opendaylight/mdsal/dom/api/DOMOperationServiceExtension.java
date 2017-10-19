/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Marker interface for extensions of {@link DOMOperationService}.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public interface DOMOperationServiceExtension
    extends DOMServiceExtension<DOMOperationService, DOMOperationServiceExtension> {

}
