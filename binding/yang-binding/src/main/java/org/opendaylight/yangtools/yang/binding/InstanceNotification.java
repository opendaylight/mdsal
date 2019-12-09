/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.annotations.Beta;

/**
 * An {@code notification} which is defined within the schema tree and is thus tied to a data tree instance.
 *
 * @param <T> Parent data tree instance type
 */
@Beta
public interface InstanceNotification<T extends DataObject> extends Notification {

}
