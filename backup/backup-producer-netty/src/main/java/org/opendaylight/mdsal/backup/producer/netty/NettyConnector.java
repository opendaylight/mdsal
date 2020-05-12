/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.backup.producer.netty;

import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyConnector {

    private static final Logger LOG = LoggerFactory.getLogger(NettyConnector.class);
    private static final short LENGTH_OF_FIELD_LENGTH = 8;

    private final ChannelHandler productionHandler;
    private final NioEventLoopGroup eventLoopGroup;
    private final NettyBackupProducerConfiguration configuration;

    private Bootstrap bootstrap = new Bootstrap();
    private Channel outputChannel;
    private Timer timer;
    private boolean closingConnector;


    public NettyConnector(NettyBackupProducerConfiguration config, Timer timer, ChannelHandler productionHandler) {
        this.configuration = Preconditions.checkNotNull(config, "Config must be provided");
        this.timer = timer;
        this.eventLoopGroup = new NioEventLoopGroup(1);
        this.productionHandler = productionHandler;
    }

    public void init() {
        bootstrap.group(eventLoopGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline()
                    .addLast("frameEncoder", new LengthFieldPrepender(LENGTH_OF_FIELD_LENGTH))
                    .addLast(productionHandler);
            }
        });
        scheduleConnect(10);
    }

    public void close() throws InterruptedException {
        closingConnector = true;
        eventLoopGroup.shutdownGracefully();
        if (outputChannel != null) {
            outputChannel.close().sync();
        }
    }

    @SuppressWarnings(value = "IllegalCatch")
    @SuppressFBWarnings(value = {"UPM_UNCALLED_PRIVATE_METHOD","REC_CATCH_EXCEPTION"},
        justification = "Called from TimerTask")
    private void doConnect() {
        try {
            final InetSocketAddress socketAddress = configuration.getClientSocketAddress();
            LOG.info("Connecting to {}", socketAddress);
            bootstrap.connect(socketAddress).addListener(new ChannelFutureListener() {
                @Override public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        future.channel().close();
                        if (closingConnector) {
                            return;
                        }
                        scheduleReconnect();
                    } else {
                        outputChannel = future.channel();
                        addCloseDetectListener(outputChannel);
                    }
                }

                private void addCloseDetectListener(Channel channel) {
                    channel.closeFuture().addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future)
                            throws Exception {
                            LOG.warn("Connection lost. Scheduling reconnect.");
                            scheduleReconnect();
                        }
                    });
                }
            });
        } catch (Exception ex) {
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {

        LOG.debug("Attempting to reconnect in {}ms", configuration.getConnectionRetryIntervalMs());
        scheduleConnect(configuration.getConnectionRetryIntervalMs());
    }

    private void scheduleConnect(long millis) {
        if (!closingConnector) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    doConnect();
                }
            }, millis);
        }
    }
}
