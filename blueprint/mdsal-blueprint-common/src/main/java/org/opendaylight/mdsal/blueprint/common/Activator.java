/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.blueprint.common;

import java.util.Dictionary;
import java.util.Hashtable;
import org.apache.aries.blueprint.NamespaceHandler;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {


    private ServiceRegistration<@NonNull NamespaceHandler> namespaceReg;

    private void registerNamespaceHandler(final BundleContext context) {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(BlueprintConstants.BLUEPRINT_NAMESPACE_PROP, CommonNamespaceHandler.NAMESPACE);

        namespaceReg = context.registerService(NamespaceHandler.class, new CommonNamespaceHandler(), props);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // TODO Auto-generated method stub

    }
}
