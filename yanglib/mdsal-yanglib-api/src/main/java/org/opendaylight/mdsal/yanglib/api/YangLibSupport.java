/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointLabel;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContextFactory;

/**
 * Main entry point into YANG (Module) Library support instance.
 */
@Beta
@NonNullByDefault
public interface YangLibSupport {
    /**
     * Create a MountPointContextFactory, backed by a specific SchemaContextResolver.
     *
     * @param label Resulting Mount Point identifier
     * @param resolver SchemaContext resolver
     * @return A new factory
     * @throws NullPointerException if any argument is null
     */
    MountPointContextFactory createMountPointContextFactory(MountPointLabel label, SchemaContextResolver resolver);

    /**
     * Return the revision date of the model this support implements. The value returned from this method is suitable
     * for reporting in <a href="https://tools.ietf.org/html/rfc8040#section-3.3.3">RFC8040 section 3.3.3</a>.
     *
     * @return A revision.
     */
    Revision implementedRevision();

    /**
     * Create a new content builder which is used to serialize yang library content into NormalizedNodes.
     * This content builder has further options which can influence the resulting content.
     *
     * @return A new yang library content builder.
     */
    YangLibraryContentBuilder newContentBuilder();
}
