/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMOperationImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcException;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

final class DOMActionRoutingTableEntry extends AbstractDOMOperationRoutingTableEntry {

    DOMActionRoutingTableEntry(final SchemaPath actionpath, final Map<YangInstanceIdentifier,
            List<DOMOperationImplementation>> impls) {
        super(actionpath, impls);
    }

    void invokeAction(@Nonnull final YangInstanceIdentifier parent, @Nullable final NormalizedNode<?, ?> input,
            @Nonnull final BiConsumer<DOMRpcResult, DOMRpcException> callback) {
        ((DOMOperationImplementation) getImplementations(parent).get(0)).invokeOperation(
            DOMRpcIdentifier.create(getSchemaPath(), parent), input, callback);
    }

    @Override
    protected DOMActionRoutingTableEntry newInstance(final Map<YangInstanceIdentifier,
            List<DOMOperationImplementation>> impls) {
        return new DOMActionRoutingTableEntry(getSchemaPath(), impls);
    }
}