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
 * A value-based match executed from some point in the data tree.
 *
 * @param <R> root of query
 */
@Beta
public interface ValueMatch<R extends DataObject> {

}
