/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import com.google.common.annotations.Beta;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMSchemaServiceExtension;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;

/**
 * Base class to implement DOMSchemaService more easily while providing a bridge between MD-SAL DOM Schema services
 * and YANG Tools Schema consumers.
 *
 * @author Michael Vorburger.ch
 */
@Beta
@NonNullByDefault
public abstract class AbstractDOMSchemaService implements DOMSchemaService, SchemaContextProvider {
    public abstract static class WithYangTextSources extends AbstractDOMSchemaService
            implements DOMYangTextSourceProvider {
        @Override
        public ClassToInstanceMap<DOMSchemaServiceExtension> getExtensions() {
            return ImmutableClassToInstanceMap.of(DOMYangTextSourceProvider.class, this);
        }
    }

    @Override
    public final SchemaContext getSchemaContext() {
        // Always route context queries to a single method
        return getGlobalContext();
    }

    @Override
    public final SchemaContext getSessionContext() {
        // This method should not be implemented
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassToInstanceMap<DOMSchemaServiceExtension> getExtensions() {
        return ImmutableClassToInstanceMap.of();
    }
}
