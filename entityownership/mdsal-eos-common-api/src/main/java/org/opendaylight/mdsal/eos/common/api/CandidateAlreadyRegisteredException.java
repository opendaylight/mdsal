/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.common.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Path;

/**
 * Thrown when a Candidate has already been registered for a given Entity. This could be due to a component doing a
 * duplicate registration or two different components within the same process trying to register a Candidate.
 */
public class CandidateAlreadyRegisteredException extends Exception {
    private static final long serialVersionUID = 1L;

    private final @NonNull GenericEntity<?> entity;

    public <T extends Path<T>> CandidateAlreadyRegisteredException(@NonNull final GenericEntity<T> entity) {
        super("Candidate has already been registered for " + entity);
        this.entity = requireNonNull(entity, "entity should not be null");
    }

    /**
     * Gets the entity for which a Candidate has already been registered in the current process.
     *
     * @param <T> the instance identifier path type
     *
     * @return the entity.
     */
    @SuppressWarnings("unchecked")
    public <T extends Path<T>> @NonNull GenericEntity<T> getEntity() {
        return (GenericEntity<T>) entity;
    }
}
