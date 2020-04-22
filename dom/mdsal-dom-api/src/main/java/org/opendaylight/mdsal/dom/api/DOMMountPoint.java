/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import java.util.Optional;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;

// FIXME: 7.0.0: EffectiveModelContextProvider is probably not accurate here -- we really want to unify access via
//               DOMSchemaService and that in turn needs to account for NMDA
public interface DOMMountPoint extends Identifiable<YangInstanceIdentifier>, EffectiveModelContextProvider {

    <T extends DOMService> Optional<T> getService(Class<T> cls);
}
