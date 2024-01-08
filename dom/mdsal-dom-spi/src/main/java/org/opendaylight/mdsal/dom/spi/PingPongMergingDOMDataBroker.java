/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;

/**
 * Utility mixin interface for {@link DOMDataBroker}s which realize merging transaction chains via
 * {@link PingPongTransactionChain}. It provides {@link #createMergingTransactionChain()}
 * as a default method combining {@link PingPongTransactionChain} with {@link #createTransactionChain()}.
 */
@Beta
public interface PingPongMergingDOMDataBroker extends DOMDataBroker {
    @Override
    default DOMTransactionChain createMergingTransactionChain() {
        return new PingPongTransactionChain(createTransactionChain());
    }
}
