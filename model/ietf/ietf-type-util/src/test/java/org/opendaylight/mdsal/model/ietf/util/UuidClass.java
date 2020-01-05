/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("checkstyle:memberName")
public final class UuidClass {
    private final String _value;

    public UuidClass(final String value) {
        this._value = requireNonNull(value);
    }

    public UuidClass(final UuidClass template) {
        this._value = template._value;
    }

    String getValue() {
        return _value;
    }
}