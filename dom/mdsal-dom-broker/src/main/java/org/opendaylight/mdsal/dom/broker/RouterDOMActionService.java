/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Singleton
@Component
@NonNullByDefault
public final class RouterDOMActionService implements DOMActionService {
    private final List<Extension> supportedExtensions;
    private final DOMRpcRouter router;

    @Inject
    @Activate
    public RouterDOMActionService(@Reference final DOMRpcRouter router) {
        this.router = requireNonNull(router);
        supportedExtensions = List.of(new RouterDOMActionAvailabilityExtension(router));
    }

    @Override
    public List<Extension> supportedExtensions() {
        return supportedExtensions;
    }

    @Override
    public ListenableFuture<? extends DOMRpcResult> invokeAction(final Absolute type, final DOMDataTreeIdentifier path,
            final ContainerNode input) {
        return router.invokeAction(type, path, input);
    }
}
