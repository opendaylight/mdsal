/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.clustering;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipCandidateRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * DOM version of {@link GenericEntityOwnershipCandidateRegistration}.
 *
 * @author Thomas Pantelis
 */
@Beta
public interface DOMEntityOwnershipCandidateRegistration extends
        GenericEntityOwnershipCandidateRegistration<YangInstanceIdentifier, DOMEntity> {
}
