/*
 * Copyright (c) 2020 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.api;

import org.opendaylight.yangtools.yang.data.api.DatastoreIdentifier;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

public interface YangLibraryContentBuilder extends LegacyYangLibraryContentBuilder {

    /**
     * Option to include legacy yang library content in the resulting output.
     *
     * @return LegacyYangLibraryContentBuilder which generates output that contains yang library data in
     *         both legacy and non-legacy format.
     */
    LegacyYangLibraryContentBuilder includeLegacy();

    @Override
    YangLibraryContentBuilder setContext(EffectiveModelContext modelContext);

    /**
     * Add a secondary datastore(s) which use different EffectiveModelContext than the default provided context.
     * This/These datastore/s are used in the output encoding of the yang library.
     *
     * @param identifier identifies the datastore in the ouput
     * @param context EffectiveModelContext of this datastore
     * @return this builder
     */
    YangLibraryContentBuilder addDatastore(DatastoreIdentifier identifier, EffectiveModelContext context);
}
