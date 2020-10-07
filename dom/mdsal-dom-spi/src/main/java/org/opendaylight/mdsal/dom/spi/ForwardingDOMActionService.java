/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.dom.api.DOMActionResult;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMActionServiceExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

@Beta
public abstract class ForwardingDOMActionService
        extends ForwardingDOMExtensibleService<DOMActionService, DOMActionServiceExtension>
        implements DOMActionService {
    @Override
    public ListenableFuture<? extends DOMActionResult> invokeAction(final Absolute type,
            final DOMDataTreeIdentifier path, final ContainerNode input) {
        return delegate().invokeAction(type, path, input);
    }
}
