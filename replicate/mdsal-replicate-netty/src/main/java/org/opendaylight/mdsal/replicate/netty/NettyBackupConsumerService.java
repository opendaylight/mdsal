/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.replicate.common.AbstractBackupConsumerService;
import org.opendaylight.mdsal.replicate.common.DTCSerializationException;
import org.opendaylight.mdsal.replicate.common.DTCSerializer;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyBackupConsumerService extends AbstractBackupConsumerService {

    private static final Logger LOG = LoggerFactory.getLogger(NettyBackupConsumerService.class);

    private static final String SINGLETON_IDENTIFIER = "NettyBackupConsumerService";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> consumptionFuture;
    private boolean closingConsumer;
    private NettyBackupConsumerConfiguration configuration;

    public NettyBackupConsumerService(final DOMDataBroker domDataBroker) {
        super(domDataBroker);
    }

    @Override
    protected void initConsumer() throws IOException {
        LOG.info("Init NettyBackupConsumerService");
        this.configuration = NettyBackupConsumerConfigurationBuilder.fromJson(getClass().getClassLoader()
            .getResourceAsStream("nettyConsumer.json"));
    }

    @Override
    protected synchronized void startConsumption() {
        LOG.info("Starting consumption");
        this.consumptionFuture = executor.submit(this::startServer);
    }

    private void startServer() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(final SocketChannel ch) throws Exception {
                        ch.pipeline()
                            .addLast("frameDecoder",
                                new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,8,0,8))
                            .addLast(new ConsumptionHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 1)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            LOG.info("Binding to port {}", configuration.getListeningPort());
            ChannelFuture future = bootstrap.bind(configuration.getListeningPort()).sync();
            Channel channel = future.channel();
            LOG.info("Listening...");
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            if (!this.closingConsumer) {
                LOG.error("Consumption was interrupted unexpectedly.", e);
                restartServer();
            }
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
        LOG.info("Consumption stopped");
    }

    private void restartServer() {
        if (configuration.isEnableServerAutoRestart()) {
            LOG.info("Restarting Consumer");
            consumptionFuture.cancel(false);
            startConsumption();
        } else {
            LOG.info("Restart won't be attempted, since enable-server-auto-restart wasn't set");
        }
    }

    @Override
    protected Boolean closeConsumer() {
        LOG.info("Shutting down consumer");
        this.closingConsumer = true;
        boolean shutdownSuccess = consumptionFuture.cancel(true);
        executor.shutdownNow();
        return shutdownSuccess;
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return ServiceGroupIdentifier.create(SINGLETON_IDENTIFIER);
    }

    private class ConsumptionHandler extends SimpleChannelInboundHandler<ByteBuf> {

        private final Logger log = LoggerFactory.getLogger(ConsumptionHandler.class);

        @Override
        protected void channelRead0(final ChannelHandlerContext channelHandlerContext,
            final ByteBuf byteBuf) throws Exception {
            log.info("Received data - {}b", byteBuf.readableBytes());
            try {
                byte[] dataReceived = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(dataReceived);
                DataTreeCandidate candidate = DTCSerializer.deserializeDTC(dataReceived);
                applyBackup(candidate);
            } catch (DTCSerializationException e) {
                log.error("Couldn't apply candidate because of deserialization error");
            }
        }

        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause)
            throws Exception {
            log.error("Error while consuming data: {}", cause.getMessage());
            ctx.close();
        }
    }
}
