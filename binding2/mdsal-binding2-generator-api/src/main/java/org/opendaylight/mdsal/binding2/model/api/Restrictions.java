/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.model.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

@Beta
public interface Restrictions {

    List<LengthConstraint> getLengthConstraints();
    List<PatternConstraint> getPatternConstraints();
    List<RangeConstraint> getRangeConstraints();
    boolean isEmpty();

}
