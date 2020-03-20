/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import com.google.common.annotations.Beta;

/**
 * Exception reported when the proposed query has a structural problem. This may be either a mismatch with underlying
 * schema, a value type problem, or a general DTO relationship issue.
 */
@Beta
public class QueryStructureException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public QueryStructureException(final String message) {
        super(message);
    }

    public QueryStructureException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
