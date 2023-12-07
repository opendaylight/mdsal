/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import java.util.Collection;
import java.util.List;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMYangTextSourceProvider;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;

/**
 * Base class to implement DOMSchemaService more easily while providing a bridge between MD-SAL DOM Schema services
 * and YANG Tools Schema consumers.
 *
 * @author Michael Vorburger.ch
 */
public abstract class AbstractDOMSchemaService implements DOMSchemaService, EffectiveModelContextProvider {
    public abstract static class WithYangTextSources extends AbstractDOMSchemaService
            implements DOMYangTextSourceProvider {
        @Override
        public Collection<? extends Extension> supportedExtensions() {
            return List.of(this);
        }
    }

    @Override
    public final EffectiveModelContext getEffectiveModelContext() {
        final var ret = getGlobalContext();
        if (ret == null) {
            throw new IllegalStateException("Global context is not available in " + this);
        }
        return ret;
    }
}
