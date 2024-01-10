/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.util.List;

public sealed interface DataObjectSteps extends List<DataObjectStep<?>> permits StackedDataObjectSteps {

    // FIXME: forward compatibility: @Override once we have Java 21
    DataObjectStep<?> getLast();
}
