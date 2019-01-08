/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.blueprint.common;

import com.google.common.annotations.Beta;

@Beta
public final class BlueprintConstants {
    // FIXME: add documentation
    public static final String DEFAULT_TYPE_FILTER = "(|(type=default)(!(type=*)))";

    public static final String BLUEPRINT_NAMESPACE_PROP = "osgi.service.blueprint.namespace";

    private BlueprintConstants() {

    }
}
