/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.Optional;
import java.util.regex.Pattern;
import org.opendaylight.mdsal.model.ietf.util.AbstractIetfYangUtil;

/**
 * Utility methods for working with types defined in ietf-yang-types.
 */
@Beta
public final class IetfYangUtil extends AbstractIetfYangUtil<MacAddress> {

    public static final IetfYangUtil INSTANCE = new IetfYangUtil();

    private Pattern uuidPattern;

    private IetfYangUtil() {
        super(MacAddress.class);
    }

    @Override
    protected String getValue(final MacAddress macAddress) {
        return macAddress.getValue();
    }

    // https://bugs.opendaylight.org/show_bug.cgi?id=7992 proposes to auto-gen. this
    public Optional<Uuid> newUuidIfValidPattern(String possibleUuid) {
        Preconditions.checkNotNull(possibleUuid, "possibleUuid == null");

        // https://bugs.opendaylight.org/show_bug.cgi?id=7991 proposes how this could be avoided
        if (uuidPattern == null) {
            // Thread safe because it really doesn't matter even if we were to do this initialization more than once
            if (Uuid.PATTERN_CONSTANTS.size() != 1) {
                throw new IllegalStateException("Uuid.PATTERN_CONSTANTS.size() != 1");
            }
            uuidPattern = Pattern.compile(Uuid.PATTERN_CONSTANTS.get(0));
        }

        if (uuidPattern.matcher(possibleUuid).matches()) {
            return Optional.of(new Uuid(possibleUuid));
        } else {
            return Optional.empty();
        }
    }

}
