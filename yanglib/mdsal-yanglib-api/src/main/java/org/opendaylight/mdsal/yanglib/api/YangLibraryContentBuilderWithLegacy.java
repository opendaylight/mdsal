/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

@Beta
public interface YangLibraryContentBuilderWithLegacy extends YangLibraryContentBuilder {
    /**
     * Format legacy content, if applicable. It is guaranteed to be a sibling of the main content returned by
     * {@link #formatYangLibraryContent()}.
     *
     * @return Legacy content, if applicable.
     */
    Optional<ContainerNode> formatYangLibraryLegacyContent();
}
