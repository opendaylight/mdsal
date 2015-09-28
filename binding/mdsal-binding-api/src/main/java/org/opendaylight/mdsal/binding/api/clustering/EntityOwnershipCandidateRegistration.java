/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.clustering;

import org.opendaylight.mdsal.common.api.clustering.GenericEntityOwnershipCandidateRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Binding version of {@link GenericEntityOwnershipCandidateRegistration}.
 *
 * @author Thomas Pantelis
 */
public interface EntityOwnershipCandidateRegistration extends
        GenericEntityOwnershipCandidateRegistration<InstanceIdentifier<?>, Entity> {
}
