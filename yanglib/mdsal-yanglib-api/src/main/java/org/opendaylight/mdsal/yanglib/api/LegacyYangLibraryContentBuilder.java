/*
 * Copyright (c) 2020 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.api;

import java.util.List;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

public interface LegacyYangLibraryContentBuilder {

    /**
     * Set the default EffectiveModelContext used for this content builder.
     *
     * @param modelContext EffectiveModelContext to use for content generation.
     * @return this builder
     */
    LegacyYangLibraryContentBuilder setContext(EffectiveModelContext modelContext);

    /**
     * Format the contents of the yang library into NormalizedNodes using the provided EffectiveModelContext.
     *
     * @return List of NormalizedNodes that contain the yang library content.
     */
    List<? extends ContainerNode> formatYangLibraryContent();
}
