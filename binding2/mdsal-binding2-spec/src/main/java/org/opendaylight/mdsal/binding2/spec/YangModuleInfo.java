/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.spec;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;

/**
 * Provides basic information about YANG module
 */
@Beta
public interface YangModuleInfo {

    /**
     * Returns yang module name
     *
     * @return YANG module name
     */
    String getName();

    /**
     * Returns revision of yang module.
     *
     * @return YANG module revision
     */
    String getRevision();

    /**
     * Returns semantic version of yang module
     *
     * @return YANG module semantic version
     */
    Optional<SemVer> getSemanticVersion();

    /**
     * Returns XML namespace associated to the YANG module
     *
     * @return XML namespace associated to the YANG module.
     */
    String getNamespace();

    /**
     * Returns set of imported modules
     * @return set of YangModuleInfo instances
     */
    Set<YangModuleInfo> getImportedModules();

    /**
     * Transforms YangModuleInfo instance to its source representation
     * @return YangModuleInfo source representation
     */
    SchemaSourceRepresentation getModuleSourceRepresentation();

}
