/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.impl;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.yanglib.ModulesStateFactory;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.ModulesState;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens for updates on global schema context, transforms context to ietf-yang-library:modules-state and
 * writes this state to operational data store.
 */
/*
 * FIXME: This needs to be refactored and integrated with DOMSchemaService, so we can emit proper yang-library-change
 *        and publish it
 */
@Beta
public final class SchemaServiceToMdsalWriter implements SchemaContextListener, Registration {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaServiceToMdsalWriter.class);
    private static final YangInstanceIdentifier MODULES_STATE_INSTANCE_IDENTIFIER =
            YangInstanceIdentifier.of(ModulesState.QNAME);

    private final ModulesStateFactory factory = new ImmutableModulesStateFactory();
    private final DOMSchemaService schemaService;
    private final DOMDataBroker dataBroker;

    @GuardedBy("this")
    private Registration reg;

    SchemaServiceToMdsalWriter(final DOMSchemaService schemaService, final DOMDataBroker dataBroker) {
        this.schemaService = requireNonNull(schemaService);
        this.dataBroker = requireNonNull(dataBroker);
    }

    @Override
    public synchronized void close() {
        if (reg == null) {
            return;
        }

        reg.close();
        reg = null;

        final DOMDataTreeWriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.delete(LogicalDatastoreType.OPERATIONAL, MODULES_STATE_INSTANCE_IDENTIFIER);
        tx.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(@Nullable final CommitInfo result) {
                LOG.debug("Modules state removed successfully with {}", result);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("Failed to remove modules state", throwable);
            }
        }, MoreExecutors.directExecutor());
    }

    /**
     * Invoked by blueprint.
     */
    public synchronized void start() {
        checkState(reg == null, "Writer already started with registration %s", reg);
        reg = schemaService.registerSchemaContextListener(this);
    }

    @Override
    public synchronized void onGlobalContextUpdated(final SchemaContext context) {
        final ContainerNode modulesState = factory.createModulesState(context);

        LOG.debug("Trying to write new module-state: {}", modulesState);
        final DOMDataTreeWriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.OPERATIONAL, MODULES_STATE_INSTANCE_IDENTIFIER, modulesState);
        tx.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(@Nullable final CommitInfo result) {
                LOG.debug("Modules state updated successfully with {}", result);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("Failed to update modules state", throwable);
            }
        }, MoreExecutors.directExecutor());
    }
}
