/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.clustering;

import javax.annotation.Nonnull;
import org.opendaylight.mdsal.common.api.clustering.EntityOwnershipChangeState;
import org.opendaylight.mdsal.common.api.clustering.GenericEntityOwnershipChange;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * DOM version of {@link GenericEntityOwnershipChange}.
 *
 * @author Thomas Pantelis
 */
public class DOMEntityOwnershipChange extends GenericEntityOwnershipChange<YangInstanceIdentifier, DOMEntity> {

    /**
     * {@inheritDoc}
     */
    public DOMEntityOwnershipChange(@Nonnull DOMEntity entity, @Nonnull EntityOwnershipChangeState state) {
        super(entity, state);
    }
}
