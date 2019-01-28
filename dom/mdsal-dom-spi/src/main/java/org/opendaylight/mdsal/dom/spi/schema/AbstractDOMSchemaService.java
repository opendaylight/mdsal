/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.schema;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMSchemaServiceExtension;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * Base class to implement DOMSchemaService more easily.
 *
 * @author Michael Vorburger.ch
 */
public abstract class AbstractDOMSchemaService
        implements SchemaContextProvider, DOMSchemaService, DOMYangTextSourceProvider {

    private final SchemaContextProvider schemaContextProvider;
    private final SchemaSourceProvider<YangTextSchemaSource> schemaSourceProvider;

    public AbstractDOMSchemaService(SchemaContextProvider schemaContextProvider,
            SchemaSourceProvider<YangTextSchemaSource> schemaSourceProvider) {
        this.schemaContextProvider = schemaContextProvider;
        this.schemaSourceProvider = schemaSourceProvider;
    }

    @Override
    public SchemaContext getSchemaContext() {
        return getGlobalContext();
    }

    @Override
    public SchemaContext getGlobalContext() {
        return schemaContextProvider.getSchemaContext();
    }

    @Override
    public SchemaContext getSessionContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NonNull ClassToInstanceMap<DOMSchemaServiceExtension> getExtensions() {
        return ImmutableClassToInstanceMap.of(DOMYangTextSourceProvider.class, this);
    }

    @Override
    public ListenableFuture<? extends YangTextSchemaSource> getSource(SourceIdentifier sourceIdentifier) {
        return schemaSourceProvider.getSource(sourceIdentifier);
    }
}
