/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.function.Consumer;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;

@NonNullByDefault
public interface DOMSchemaService extends DOMService<DOMSchemaService, DOMSchemaService.Extension> {
    /**
     * Type capture of a {@link DOMService.Extension} applicable to {@link DOMSchemaService} implementations.
     */
    interface Extension extends DOMService.Extension<DOMSchemaService, Extension> {
        // Marker interface
    }

    /**
     * Returns global schema context.
     *
     * @return schemaContext
     */
    EffectiveModelContext getGlobalContext();

    /**
     * Register a listener for changes in schema context.
     *
     * @param listener Listener which should be registered
     * @return Listener registration handle
     * @throws NullPointerException if {@code listener} is {@code null}
     */
    Registration registerSchemaContextListener(Consumer<EffectiveModelContext> listener);

    /**
     * An {@link Extension} exposing access to {@link YangTextSource}.
     */
    @FunctionalInterface
    interface YangTextSourceExtension extends Extension {
        /**
         * Return a future producing a {@link YangTextSource} containing the YANG text of specified source.
         *
         * @param sourceId A {@link SourceIdentifier}
         * @return A future
         */
        ListenableFuture<YangTextSource> getYangTexttSource(SourceIdentifier sourceId);
    }
}
