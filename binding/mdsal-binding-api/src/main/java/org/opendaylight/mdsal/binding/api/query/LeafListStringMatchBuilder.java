/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Builder for a match of a String leaf value.
 *
 * @param <T> query result type
 */
@Beta
public interface LeafListStringMatchBuilder<T extends DataObject> extends LeafListMatchBuilder<T, String> {
    @Override
    ContainsStringMatchBuilder<T> contains();

    @Override
    ContainsStringMatchBuilder<T> allMatch();

    @Override
    ContainsStringMatchBuilder<T> anyMatch();

    @Override
    ContainsStringMatchBuilder<T> noneMatch();
}
