/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.model.ietf.util.AbstractIetfYangUtil;

/**
 * Utility methods for working with types defined in ietf-yang-types.
 */
@Beta
public final class IetfYangUtil extends AbstractIetfYangUtil<MacAddress> {
    public static final IetfYangUtil INSTANCE = new IetfYangUtil();

    private IetfYangUtil() {
        super(MacAddress.class);
    }

    @Override
    protected String getValue(final MacAddress macAddress) {
        return macAddress.getValue();
    }
}
