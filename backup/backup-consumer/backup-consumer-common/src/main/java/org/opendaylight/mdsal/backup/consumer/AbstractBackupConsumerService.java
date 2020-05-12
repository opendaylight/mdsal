/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.backup.consumer;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for any BackupConsumerService utilizing any transport system. When extending this class
 * the methods initConsumer(), startConsumption() and closeConsumer() have to be implemented specifically to the
 * transport system's APIs.
 * After receiving a modification, the data must be deserialized and method applyBackup(DataTreeCandidate) can be
 * called which writes the data into datastore.
 */
public abstract class AbstractBackupConsumerService implements ClusterSingletonService {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractBackupConsumerService.class);
    private DOMDataBroker domDataBroker;

    public AbstractBackupConsumerService(final DOMDataBroker domDataBroker) {
        this.domDataBroker = domDataBroker;
    }

    protected abstract void initConsumer() throws IOException;

    protected abstract void startConsumption();

    protected abstract Boolean closeConsumer();

    protected final void applyBackup(@NonNull final DataTreeCandidate candidate) {
        DOMDataTreeWriteTransaction writeTransaction = domDataBroker.newWriteOnlyTransaction();
        final BackupModification modification = new BackupModification(writeTransaction);
        DataTreeCandidates.applyToModification(modification, candidate);
        //LOG.info("Commit backup writeTransaction - NOT REALLY THO, JUST TESTING");
        try {
            LOG.info("Commit backup writeTransaction");
            writeTransaction.commit().get(10, TimeUnit.SECONDS);

        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error while commiting backup transaction", e);
        } catch (TimeoutException e) {
            LOG.error("TimeoutException while commiting backup transaction", e);
        }
        writeTransaction.cancel();
    }

    @Override
    public void instantiateServiceInstance() {
        try {
            initConsumer();
        } catch (IOException e) {
            LOG.error("Backup Consumer initialization failed.", e);
            return;
        }
        startConsumption();
    }

    @Override
    public ListenableFuture<? extends Object> closeServiceInstance() {
        Boolean shutdownSuccess = closeConsumer();
        return Futures.immediateFuture(shutdownSuccess);
    }
}
