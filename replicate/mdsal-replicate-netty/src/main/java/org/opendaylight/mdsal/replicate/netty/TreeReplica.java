/*
 * Copyright (c) 2026 SmartOptics AS and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FutureCallback;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.lock.qual.Holding;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeDataOutput;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeStreamVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TreeReplica extends ChannelInitializer<SocketChannel> {
    private static final long CHANNEL_CLOSE_TIMEOUT_S = 10;
    private static final Logger LOG =
            LoggerFactory.getLogger(TreeReplica.class);

    private final DOMDataTreeIdentifier tree;
    private final ByteBuf treeRequest;
    private final BootstrapSupport bootstrapSupport;
    private final DOMDataBroker dataBroker;
    private final InetSocketAddress sourceAddress;
    private final Duration reconnectDelay;
    private final int maxMissedKeepalives;
    private final Duration keepaliveInterval;

    @GuardedBy("this")
    private ChannelFuture futureChannel;
    @GuardedBy("this")
    private Bootstrap bootstrap;
    @GuardedBy("this")
    private boolean closingInstance;

    TreeReplica(NettyReplicationConfig config, DOMDataTreeIdentifier tree) {
        this.bootstrapSupport = config.bootstrapSupport();
        this.dataBroker = config.dataBroker();
        this.sourceAddress = config.sourceAddress();
        this.reconnectDelay = config.reconnectDelay();
        this.keepaliveInterval = config.keepaliveInterval();
        this.maxMissedKeepalives = config.maxMissedKeepalives();
        this.tree = requireNonNull(tree);

        try {
            treeRequest = Unpooled.unreleasableBuffer(requestTree(tree));
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Failed to create subscribe request for tree " + tree,
                    e);
        }
    }

    synchronized boolean close() {
        closingInstance = true;
        return stop();
    }

    synchronized void start() {
        closingInstance = false;
        if (bootstrap == null) {
            bootstrap = bootstrapSupport.newBootstrap();
        }
        doConnect();
    }

    synchronized boolean stop() {
        if (futureChannel == null) {
            return true;
        }

        boolean shutdownSuccess = true;
        final Channel channel = futureChannel.channel();
        if (channel != null && channel.isActive()) {
            try {
                // NOTE: blocking await() on event loop may be problematic; left as-is for compatibility
                channel.close().await(
                        CHANNEL_CLOSE_TIMEOUT_S,
                        TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOG.error(
                        "The channel for tree {} didn't close properly "
                                + "within {} seconds",
                        tree,
                        CHANNEL_CLOSE_TIMEOUT_S);
                Thread.currentThread().interrupt();
                shutdownSuccess = false;
            }
        }

        shutdownSuccess &= futureChannel.cancel(true);
        futureChannel = null;
        return shutdownSuccess;
    }

    @Holding("SinkSingletonService.this")
    private void doConnect() {
        LOG.info("Replication sink started with source {} for tree {}", sourceAddress, tree);
        final ScheduledExecutorService group = bootstrap.config().group();

        futureChannel = bootstrap
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(this)
                .connect(sourceAddress, null);
        futureChannel.addListener(
                (ChannelFutureListener) future -> channelResolved(future, group));
    }

    private synchronized boolean isClosingInstance() {
        return closingInstance;
    }

    synchronized void reconnect() {
        if (isClosingInstance()) {
            return;
        }
        stop();
        doConnect();
    }

    @Override
    protected void initChannel(final SocketChannel ch) {
        final var txChain = dataBroker.createMergingTransactionChain();

        ch.pipeline()
                .addLast("frameDecoder", new MessageFrameDecoder())
                .addLast(
                        "idleStateHandler",
                        new IdleStateHandler(
                                keepaliveInterval.toNanos() * maxMissedKeepalives,
                                0,
                                0,
                                TimeUnit.NANOSECONDS))
                .addLast("keepaliveHandler", new SinkKeepaliveHandler())
                .addLast("requestHandler", new SinkRequestHandler(tree, txChain))
                .addLast("frameEncoder", MessageFrameEncoder.INSTANCE);

        txChain.addCallback(new FutureCallback<>() {
            @Override
            public void onSuccess(final Empty result) {
                LOG.info(
                        "Transaction chain for channel {} and tree {} completed",
                        ch,
                        tree);
            }

            @Override
            public void onFailure(final Throwable cause) {
                LOG.error(
                        "Transaction chain for channel {} and tree {} failed",
                        ch,
                        tree,
                        cause);
                ch.close();
            }
        });
    }

    private synchronized void channelResolved(final ChannelFuture completedFuture,
                                              final ScheduledExecutorService group) {
        if (futureChannel != null
                && futureChannel.channel() == completedFuture.channel()) {
            if (completedFuture.isSuccess()) {
                final Channel ch = completedFuture.channel();
                LOG.info("Channel {} established for tree {}", ch, tree);
                ch.closeFuture().addListener(
                        (ChannelFutureListener) future ->
                                channelClosed(future, group));
                ch.writeAndFlush(treeRequest.duplicate());
            } else if (!isClosingInstance()) {
                LOG.info(
                        "Failed to connect to source {} for tree {}, "
                                + "reconnecting in {}",
                        sourceAddress,
                        tree,
                        reconnectDelay.getSeconds(),
                        completedFuture.cause());
                group.schedule(
                        this::reconnect,
                        reconnectDelay.toNanos(),
                        TimeUnit.NANOSECONDS);
            }
        }
    }

    private synchronized void channelClosed(final ChannelFuture completedFuture, final ScheduledExecutorService group) {
        if (futureChannel != null
                && futureChannel.channel() == completedFuture.channel()
                && !isClosingInstance()) {
            LOG.info(
                    "Channel {} lost connection to source {} for tree {}, "
                            + "reconnecting in {}",
                    completedFuture.channel(),
                    sourceAddress,
                    tree,
                    reconnectDelay.getSeconds());
            group.schedule(
                    this::reconnect,
                    reconnectDelay.toNanos(),
                    TimeUnit.NANOSECONDS);
        }
    }

    private static ByteBuf requestTree(final DOMDataTreeIdentifier tree)
            throws IOException {
        final ByteBuf ret = Unpooled.buffer();

        try (ByteBufOutputStream stream = new ByteBufOutputStream(ret)) {
            stream.writeByte(Constants.MSG_SUBSCRIBE_REQ);
            try (NormalizedNodeDataOutput output =
                         NormalizedNodeStreamVersion.current().newDataOutput(stream)) {
                tree.datastore().writeTo(output);
                output.writeYangInstanceIdentifier(tree.path());
            }
        }

        return ret;
    }
}