/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;

/**
 * An entity which can assemble a set of sources into a SchemaContext. Note that unlike other variations on this
 * theme, the definitions are done via {@link SourceReference} and can thus contain locations where a particular
 * source can be located.
 *
 * <p>
 * It is left an implementation detail whether the locations provided are checked or if the SchemaContext is resolved
 * using some other mechanism.
 */
@Beta
@NonNullByDefault
public interface SchemaContextResolver {

    SchemaContext resolveSchemaContext(List<SourceReference> librarySources, List<SourceReference> requiredSources)
            throws YangParserException;
}
