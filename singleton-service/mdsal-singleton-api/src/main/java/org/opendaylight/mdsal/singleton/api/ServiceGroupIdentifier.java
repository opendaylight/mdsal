/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifier;

/**
 * An {@link Identifier} of a group of {@link ClusterSingletonService}s.
 *
 * @param value String value, must not be {@link String#isBlank()}
 */
public record ServiceGroupIdentifier(@NonNull String value) implements Identifier {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public ServiceGroupIdentifier {
        if (value.isBlank()) {
            throw new IllegalArgumentException("Value must not be blank");
        }
    }
}
