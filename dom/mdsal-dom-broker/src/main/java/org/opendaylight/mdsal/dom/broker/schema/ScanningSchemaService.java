/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker.schema;

import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.yangtools.util.ListenerRegistry;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolver;

/**
 * Holder of singleton variables for JarScanningSchemaService instances.
 */
public interface ScanningSchemaService
        extends DOMSchemaService, SchemaContextProvider, DOMYangTextSourceProvider, AutoCloseable {

    YangTextSchemaContextResolver CONTEXT_RESOLVER = YangTextSchemaContextResolver.create("global-bundle");
    ListenerRegistry<SchemaContextListener> LISTENERS = new ListenerRegistry<>();
}
