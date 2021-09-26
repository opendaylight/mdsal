/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.osgi;

import org.opendaylight.mdsal.binding.api.config.ConfigurationListener;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;


final class ConfigurationComponentBridge<M extends DataRoot, T extends ChildOf<M>> implements ConfigurationListener<T> {
    ConfigurationComponentBridge(final Class<? extends DataObject> type) {

    }

    @Override
    public void onConfiguration(final T configuration) {
        // TODO Auto-generated method stub

    }

}
