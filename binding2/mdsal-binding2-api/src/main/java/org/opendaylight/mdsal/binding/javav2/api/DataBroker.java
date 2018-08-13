/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.AsyncDataBroker;
import org.opendaylight.mdsal.common.api.TransactionChainFactory;
import org.opendaylight.mdsal.common.api.TransactionChainListener;

/**
 * Provides access to a conceptual data tree store and also provides the ability to
 * subscribe for changes to data under a given branch of the tree.
 *
 * <p>
 * For more information on usage, please see the documentation in {@link AsyncDataBroker}.
 *
 * @see AsyncDataBroker
 * @see TransactionChainFactory
 */
@Beta
public interface DataBroker extends AsyncDataBroker<InstanceIdentifier<?>, TreeNode>, BindingService,
        TransactionFactory, DataTreeService, TransactionChainFactory<InstanceIdentifier<?>, TreeNode> {

    @Override
    BindingTransactionChain createTransactionChain(TransactionChainListener listener);
}
