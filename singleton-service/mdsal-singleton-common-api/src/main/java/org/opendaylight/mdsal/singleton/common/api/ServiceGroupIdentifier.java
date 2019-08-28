/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.common.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.AbstractStringIdentifier;

/**
 * Identifier represents a service group competence. It's based on String.
 */
public class ServiceGroupIdentifier extends AbstractStringIdentifier<ServiceGroupIdentifier> {
    private static final long serialVersionUID = 6853612584804702662L;

    protected ServiceGroupIdentifier(final @NonNull String string) {
        super(string);
    }

    /**
     * Method to create immutable instances of {@link ServiceGroupIdentifier}.
     *
     * @param name the String identifier for the ServiceGroupIdentifier instance
     * @return {@link ServiceGroupIdentifier} new instance
     */
    public static @NonNull ServiceGroupIdentifier create(final String name) {
        return new ServiceGroupIdentifier(name);
    }

    public final @NonNull String getName() {
        return getValue();
    }
}
