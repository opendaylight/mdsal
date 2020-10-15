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
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContextFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.DatastoreIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * Main entrypoint into YANG (Module) Library support instance.
 */
@Beta
@NonNullByDefault
public interface YangLibSupport {
    /**
     * Create a MountPointContextFactory, backed by a specific SchemaContextResolver.
     *
     * @param mountId Resulting Mount Point identitifer
     * @param resolver SchemaContext resolver
     * @return A new factory
     * @throws NullPointerException if any argument is null
     */
    MountPointContextFactory createMountPointContextFactory(MountPointIdentifier mountId,
            SchemaContextResolver resolver);

    /**
     * Return the revision date of the model this support implements. The value returned from this method is suitable
     * for reporting in <a href="https://tools.ietf.org/html/rfc8040#section-3.3.3">RFC8040 section 3.3.3</a>.
     *
     * @return A revision.
     */
    Revision implementedRevision();

    /**
     * Return the datastore content corresponding to a EffectiveModelContext for a set of datastores.
     *
     * @param context Effective model to report
     * @param datastores Datastores to report
     * @return List of top-level container nodes. The first item is the native container, others may be provided
     *         for compatibility.
     * @throws NullPointerException if any argument is null
     */
    List<? extends ContainerNode> formatSchema(EffectiveModelContext context, Set<DatastoreIdentifier> datastores);
}
