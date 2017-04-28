/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * A DOMSchemaServiceExtension exposing access to {@link YangTextSchemaSource}. Instances of this method should be
 * acquired from {@link DOMSchemaService}.
 */
@Beta
public interface DOMYangTextSourceProvider extends DOMSchemaServiceExtension,
        SchemaSourceProvider<YangTextSchemaSource> {

}
