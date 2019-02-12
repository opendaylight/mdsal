/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.model.ietf.util;

import static java.util.Objects.requireNonNull;

public class IpClass {
    private final String _value;

    public IpClass(final String value) {
        this._value = requireNonNull(value);
    }

    public IpClass(final IpClass template) {
        this._value = template._value;
    }

    String getValue() {
        return this._value;
    }
}