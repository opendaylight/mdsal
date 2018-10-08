/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.dom.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipChangeState;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipChange;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * DOM version of {@link GenericEntityOwnershipChange}.
 *
 * @author Thomas Pantelis
 */
@Beta
public class DOMEntityOwnershipChange extends GenericEntityOwnershipChange<YangInstanceIdentifier, DOMEntity> {
    public DOMEntityOwnershipChange(final @NonNull DOMEntity entity, final @NonNull EntityOwnershipChangeState state) {
        super(entity, state, false);
    }

    public DOMEntityOwnershipChange(final @NonNull DOMEntity entity, final @NonNull EntityOwnershipChangeState state,
            final boolean inJeopardy) {
        super(entity, state, inJeopardy);
    }
}
