/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.backup.producer.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.opendaylight.mdsal.backup.producer.AbstractBackupProducerService;
import org.opendaylight.mdsal.backup.utils.DTCSerializer;
import org.opendaylight.mdsal.backup.utils.exceptions.DTCSerializationException;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClusteredDOMDataTreeChangeListener which listens on the root of CONFIG dataTree. This means that any modification
 * to the dataTree is caught and sent to the Netty channel. There should be a BackupConsumer running on the backup
 * site which is listening on that channel. The consumer will then apply the received modifications to the
 * backup site.
 * */
public class NettyBackupProducerService extends AbstractBackupProducerService {

    private static final Logger LOG = LoggerFactory.getLogger(NettyBackupProducerService.class);

    private static final String SINGLETON_IDENTIFIER = "NettyBackupProducerService";

    private final LinkedList<DataTreeCandidate> candidateQueue = new LinkedList<>();
    private final ExecutorService executorProducer = Executors.newSingleThreadExecutor();
    private NettyConnector nettyConnector;
    private Timer timer;
    private ProductionHandler productionHandler;
    private NettyBackupProducerConfiguration configuration;

    public NettyBackupProducerService(final DOMDataBroker domDataBroker) {
        super(domDataBroker);
    }

    @Override
    protected void initProducer() throws IOException {
        LOG.info("Init NettyBackupProducerService");
        this.configuration = NettyBackupProducerConfigurationBuilder.fromJson(getClass().getClassLoader()
            .getResourceAsStream("nettyProducer.json"));
        this.timer = new Timer();
        this.productionHandler = new ProductionHandler();
        this.nettyConnector = new NettyConnector(configuration, timer, productionHandler);
        this.nettyConnector.init();
    }

    @Override
    protected void sendToBackup(final DataTreeCandidate candidate) {
        candidateQueue.add(candidate);
    }

    @Override
    protected synchronized Boolean closeBackupProducer() {
        timer.cancel();
        try {
            nettyConnector.close();
        } catch (InterruptedException e) {
            LOG.error("Failed to close NettyConnector");
            return false;
        }
        executorProducer.shutdownNow();
        LOG.info("Backup Producer closed, there was {} modifications pending to be sent", this.candidateQueue.size());
        return true;
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return ServiceGroupIdentifier.create(SINGLETON_IDENTIFIER);
    }

    @ChannelHandler.Sharable
    private class ProductionHandler extends SimpleChannelInboundHandler<ByteBuf> {

        private final Logger log = LoggerFactory.getLogger(ProductionHandler.class);

        private ScheduledFuture<?> channelActiveFuture;

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
            log.info("Readable bytes: {}",byteBuf.readableBytes());
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            log.info("Connection established. Backup production has started.");
            channelActiveFuture = ctx.channel().eventLoop().scheduleWithFixedDelay(() -> {
                while (!candidateQueue.isEmpty()) {
                    final DataTreeCandidate candidate = candidateQueue.removeFirst();
                    try {
                        byte[] serializedCandidate = DTCSerializer.serializeDTC(candidate);
                        sendData(ctx, serializedCandidate);
                    } catch (DTCSerializationException e) {
                        log.error("Serialization error. Couldn't Send DataTreeCandidate: {}",
                            candidate.getRootNode().getIdentifier(), e);
                        // this shouldn't happen
                    } catch (InterruptedException | ExecutionException interruptEx) {
                        log.error("Failed to send candidate. Connection might be lost or the process was interrupted."
                            + "Candidate will be sent again");
                        candidateQueue.addFirst(candidate);
                        break;
                    }
                }
            }, 1, 1, TimeUnit.SECONDS);
        }

        private void sendData(final ChannelHandlerContext ctx, final byte[] data)
            throws ExecutionException, InterruptedException {
            ByteBuf bufdata = ctx.channel().alloc().directBuffer(data.length);
            bufdata.writeBytes(data);
            log.info("Sending data for backup - {}b", data.length);
            ctx.writeAndFlush(bufdata).get();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            if (null != channelActiveFuture) {
                channelActiveFuture.cancel(false);
            }

            log.info("Connection closed. Backup production has stopped");
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("Exception caught: {}", cause.getMessage());
            ctx.channel().close();
        }
    }
}
