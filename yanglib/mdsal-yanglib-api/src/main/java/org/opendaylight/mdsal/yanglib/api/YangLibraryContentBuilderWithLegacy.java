/*
 * Copyright (c) 2020 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.api;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

public interface YangLibraryContentBuilderWithLegacy extends YangLibraryContentBuilder {

    /**
     *  Format the contents of the YANG library into NormalizedNodes in legacy format
     *  using the provided EffectiveModelContext
     *
     *  @return NormalizedNode with the yang library contents in legacy format
     */
    Optional<ContainerNode> formatYangLibraryLegacyContent();
}
