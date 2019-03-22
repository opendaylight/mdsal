/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("checkstyle:memberName")
public final class PhysClass {
    private final String _value;

    public PhysClass(final String value) {
        this._value = requireNonNull(value);
    }

    public PhysClass(final PhysClass template) {
        this._value = template._value;
    }

    String getValue() {
        return _value;
    }
}