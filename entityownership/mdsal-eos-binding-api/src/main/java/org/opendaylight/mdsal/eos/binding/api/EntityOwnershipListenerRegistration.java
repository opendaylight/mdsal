/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.binding.api;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Binding interface for GenericEntityOwnershipCandidateRegistration.
 *
 * @author Thomas Pantelis
 */
@Beta
public interface EntityOwnershipListenerRegistration extends
        GenericEntityOwnershipListenerRegistration<InstanceIdentifier<?>, EntityOwnershipListener> {
}
