/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.model.ietf.util;

import com.google.common.base.Preconditions;

public final class MacClass {
    final String _value;

    public MacClass(final String value) {
        this._value = Preconditions.checkNotNull(value);
    }

    public MacClass(final MacClass template) {
        this._value = template._value;
    }
}