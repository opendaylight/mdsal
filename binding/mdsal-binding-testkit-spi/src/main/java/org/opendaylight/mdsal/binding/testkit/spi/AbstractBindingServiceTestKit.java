/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testkit.spi;

import com.google.common.annotations.Beta;
import java.util.Set;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.testkit.DOMServiceTestKit;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

@Beta
public class AbstractBindingServiceTestKit extends DOMServiceTestKit {

    protected AbstractBindingServiceTestKit() {

    }

    protected AbstractBindingServiceTestKit(final Set<LogicalDatastoreType> datastoreTypes) {
        super(datastoreTypes);
    }

    protected AbstractBindingServiceTestKit(final Set<LogicalDatastoreType> datastoreTypes,
            final DOMTreeChangeListenerClassifier classifier) {
        super(datastoreTypes, classifier);
    }

    protected AbstractBindingServiceTestKit(final Set<YangModuleInfo> moduleInfos,
            final Set<LogicalDatastoreType> datastoreTypes) {
        super(moduleInfos, datastoreTypes);
    }

    protected AbstractBindingServiceTestKit(final Set<YangModuleInfo> moduleInfos,
            final Set<LogicalDatastoreType> datastoreTypes, final DOMTreeChangeListenerClassifier classifier) {
        super(moduleInfos, datastoreTypes, classifier);
    }
}
