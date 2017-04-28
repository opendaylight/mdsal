/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

/**
 * A DOMSchemaServiceExtension exposing access to {@link YangTextSchemaSource}. Instances of this method should be
 * acquired from {@link DOMSchemaService}.
 */
@Beta
public interface DOMYangTextSourceProvider extends DOMSchemaServiceExtension {
    /**
     * Returns a representation a for supplied YANG source identifier. The resolution
     * criteria are as follows:
     *
     * <ul>
     * <li>If the source identifier specifies a revision, this method returns either
     * a representation of that particular revision or throw {@link MissingSchemaSourceException}.
     * <li> If the source identifier does not specify a revision, this method returns
     * the newest available revision, or throws {@link MissingSchemaSourceException}.
     * </ul>
     *
     * In either case the returned representation is required to report a non-null
     * revision in the {@link SourceIdentifier} returned from
     * {@link SchemaSourceRepresentation#getIdentifier()}.
     *
     * Implementations are not required to provide constant behavior in time, notably
     * this different invocation of this method may produce different results.
     *
     * @param sourceIdentifier source identifier
     * @return source representation if supplied YANG module is available
     */
    ListenableFuture<YangTextSchemaSource> getYangTextSource(SourceIdentifier sourceIdentifier);
}
