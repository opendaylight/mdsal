/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.singleton.common.api;

import org.opendaylight.yangtools.util.AbstractStringIdentifier;

/**
 * Identifier represents a service group competence. It's based on String.
 */
public class ServiceGroupIdentifier extends AbstractStringIdentifier<ServiceGroupIdentifier> {

    /**
     * Method create immutable instance of {@link ServiceGroupIdentifier}
     *
     * @param serviceGroupIdentifier
     * @return {@link ServiceGroupIdentifier} instance
     */
    public static ServiceGroupIdentifier create(final String serviceGroupIdentifier) {
        return new ServiceGroupIdentifier(serviceGroupIdentifier);
    }

    protected ServiceGroupIdentifier(final String string) {
        super(string);
    }

    private static final long serialVersionUID = 6853612584804702662L;

}
