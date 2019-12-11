/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.testkit;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.mdsal.dom.broker.DOMRpcRouter;
import org.opendaylight.mdsal.dom.broker.SerializedDOMDataBroker;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.testkit.spi.AbstractDOMTestKit;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

/**
 * A testkit which creates on-demand common {@link DOMService}s for use with tests which need fully operational
 * instances.
 *
 * @author Robert Varga
 */
@Beta
public class DOMTestKit extends AbstractDOMTestKit<SerializedDOMDataBroker> {
    private volatile DOMRpcRouter domRpcRouter;

    public DOMTestKit() {

    }

    public DOMTestKit(final Set<LogicalDatastoreType> datastoreTypes) {
        super(datastoreTypes);
    }

    public DOMTestKit(final Set<LogicalDatastoreType> datastoreTypes, final DOMListenerClassifier classifier) {
        super(datastoreTypes, classifier);
    }

    public DOMTestKit(final Set<YangModuleInfo> moduleInfos, final Set<LogicalDatastoreType> datastoreTypes) {
        super(moduleInfos, datastoreTypes);
    }

    public DOMTestKit(final Set<YangModuleInfo> moduleInfos, final Set<LogicalDatastoreType> datastoreTypes,
            final DOMListenerClassifier classifier) {
        super(moduleInfos, datastoreTypes, classifier);
    }

    @Override
    public final DOMActionProviderService domActionProviderService() {
        return rpcRouter().getActionProviderService();
    }

    @Override
    public final DOMActionService domActionService() {
        return rpcRouter().getActionService();
    }

    @Override
    public final DOMRpcProviderService domRpcProviderService() {
        return rpcRouter().getRpcProviderService();
    }

    @Override
    public final DOMRpcService domRpcService() {
        return rpcRouter().getRpcService();
    }

    @Override
    protected final SerializedDOMDataBroker createDomDataBroker(final Map<LogicalDatastoreType, DOMStore> datastores) {
        return new SerializedDOMDataBroker(datastores, MoreExecutors.newDirectExecutorService());
    }

    @Override
    protected final void closeDomDataBroker(final SerializedDOMDataBroker dataBroker) {
        dataBroker.close();
    }

    private DOMRpcRouter rpcRouter() {
        DOMRpcRouter local = domRpcRouter;
        if (local == null) {
            synchronized (this) {
                local = domRpcRouter;
                if (local == null) {
                    domRpcRouter = local = DOMRpcRouter.newInstance(domSchemaService());
                }
            }
        }
        return local;
    }
}
