/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.schema;

import static java.util.Collections.singletonList;

import java.net.URI;
import java.util.List;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * Service which lets applications register YANG models dynamically.
 *
 * <p>This could be done e.g. by loading YANG model files from a folder
 * once at start-up, or continously by watching a directory.  It could
 * also be used to load YANG models from remote sources.
 *
 * @author Michael Vorburger.ch
 */
public interface YangRegisterer {

    List<Registration> registerYANGs(List<URI> yangs);

    default Registration registerYANG(URI yangURI) {
        return registerYANGs(singletonList(yangURI)).get(0);
    }
}
