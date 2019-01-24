/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.schema;

import java.util.Optional;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * Provider of schema related wirings.
 *
 * @author Michael Vorburger.ch
 */
public interface SchemaWiring {

    SchemaContextProvider getSchemaContextProvider();

    SchemaSourceProvider<YangTextSchemaSource> getSchemaSourceProvider();

    DOMSchemaService getDOMSchemaService();

    DOMYangTextSourceProvider getDOMYangTextSourceProvider();

    Optional<YangRegisterer> getYangRegisterer();

}
