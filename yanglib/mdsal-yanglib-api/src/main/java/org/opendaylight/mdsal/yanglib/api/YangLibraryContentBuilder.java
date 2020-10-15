/*
 * Copyright (c) 2020 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.DatastoreIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

@Beta
@NonNullByDefault
public interface YangLibraryContentBuilder {
    /**
     * Add a secondary datastore(s) which use different EffectiveModelContext than the default provided context.
     * This/These datastore/s are used in the output encoding of the YANG library.
     *
     * @param identifier identifies the datastore in the ouput
     * @param context EffectiveModelContext of this datastore
     * @return this builder
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if this implementation does not support per-datastore contexts and a conflicting
     *                                  context is already present.
     */
    YangLibraryContentBuilder addDatastore(DatastoreIdentifier identifier, EffectiveModelContext context);

    /**
     * Option to include legacy YANG library content in the resulting output.
     *
     * @return LegacyYangLibraryContentBuilder which generates output that contains YANG library data in
     *         both legacy and non-legacy format.
     */
    YangLibraryContentBuilder includeLegacy();

    /**
     * Format the contents of the YANG library into NormalizedNodes using the provided EffectiveModelContext.
     *
     * @return List of NormalizedNodes that contain the YANG library content.
     */
    List<? extends ContainerNode> formatYangLibraryContent();
}
