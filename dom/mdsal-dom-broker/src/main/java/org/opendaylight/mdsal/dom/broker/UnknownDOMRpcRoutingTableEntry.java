/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import java.util.List;
import java.util.Map;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

final class UnknownDOMRpcRoutingTableEntry extends AbstractDOMRpcRoutingTableEntry {
    UnknownDOMRpcRoutingTableEntry(final SchemaPath schemaPath, final Map<YangInstanceIdentifier,
            List<DOMRpcImplementation>> impls) {
        super(DOMRpcIdentifier.create(schemaPath), impls);
    }

    @Override
    protected UnknownDOMRpcRoutingTableEntry newInstance(final Map<YangInstanceIdentifier,
            List<DOMRpcImplementation>> impls) {
        return new UnknownDOMRpcRoutingTableEntry(getType(), impls);
    }
}
