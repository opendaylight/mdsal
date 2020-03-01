/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.query;

import com.google.common.annotations.Beta;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.concepts.CheckedBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

@Beta
public final class DOMQueryBuilder implements CheckedBuilder<DOMQuery, IllegalStateException> {
    private final List<DOMQueryPredicate> predicates = new ArrayList<>();

    private YangInstanceIdentifier queryRoot;
    private YangInstanceIdentifier querySelect;



    @Override
    public DOMQuery build() {
        // TODO Auto-generated method stub
        return null;
    }
}
