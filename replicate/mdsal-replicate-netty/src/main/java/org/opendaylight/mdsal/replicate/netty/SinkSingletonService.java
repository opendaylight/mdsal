/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
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
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeDataOutput;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeStreamVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SinkSingletonService extends ChannelInitializer<SocketChannel> implements ClusterSingletonService {
    private static final Logger LOG = LoggerFactory.getLogger(SinkSingletonService.class);
    private static final ServiceGroupIdentifier SGID = new ServiceGroupIdentifier(SinkSingletonService.class.getName());
    // TODO: allow different trees?
    private static final DOMDataTreeIdentifier TREE = new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION,
        YangInstanceIdentifier.of());
    private static long CHANNEL_CLOSE_TIMEOUT_S = 10;
    private static final ByteBuf TREE_REQUEST;

    static {
        try {
            TREE_REQUEST = Unpooled.unreleasableBuffer(requestTree(TREE));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final BootstrapSupport bootstrapSupport;
    private final DOMDataBroker dataBroker;
    private final InetSocketAddress sourceAddress;
    private final Duration reconnectDelay;
    private final int maxMissedKeepalives;
    private final Duration keepaliveInterval;

    @GuardedBy("this")
    private ChannelFuture futureChannel;
    private boolean closingInstance;
    private Bootstrap bs;

    SinkSingletonService(final BootstrapSupport bootstrapSupport, final DOMDataBroker dataBroker,
            final InetSocketAddress sourceAddress, final Duration reconnectDelay, final Duration keepaliveInterval,
            final int maxMissedKeepalives) {
        this.bootstrapSupport = requireNonNull(bootstrapSupport);
        this.dataBroker = requireNonNull(dataBroker);
        this.sourceAddress = requireNonNull(sourceAddress);
        this.reconnectDelay = requireNonNull(reconnectDelay);
        this.keepaliveInterval = requireNonNull(keepaliveInterval);
        this.maxMissedKeepalives = maxMissedKeepalives;
        LOG.info("Replication sink from {} waiting for cluster-wide mastership", sourceAddress);
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return SGID;
    }

    @Override
    public synchronized void instantiateServiceInstance() {
        LOG.info("Replication sink started with source {}", sourceAddress);
        bs = bootstrapSupport.newBootstrap();
        doConnect();
    }

    @Holding("this")
    private void doConnect() {
        LOG.info("Connecting to Source");
        final ScheduledExecutorService group = bs.config().group();

        futureChannel = bs
            .option(ChannelOption.SO_KEEPALIVE, true)
            .handler(this)
            .connect(sourceAddress, null);
        futureChannel.addListener((ChannelFutureListener) future -> channelResolved(future, group));
    }

    @Override
    public synchronized ListenableFuture<?> closeServiceInstance() {
        closingInstance = true;
        if (futureChannel == null) {
            return FluentFutures.immediateNullFluentFuture();
        }

        return FluentFutures.immediateBooleanFluentFuture(disconnect());
    }

    private synchronized void reconnect() {
        disconnect();
        doConnect();
    }

    private synchronized boolean disconnect() {
        boolean shutdownSuccess = true;
        final Channel channel = futureChannel.channel();
        if (channel != null && channel.isActive()) {
            try {
                // close the resulting channel. Even when this triggers the closeFuture, it won't try to reconnect since
                // the closingInstance flag is set
                channel.close().await(CHANNEL_CLOSE_TIMEOUT_S, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOG.error("The channel didn't close properly within {} seconds", CHANNEL_CLOSE_TIMEOUT_S);
                shutdownSuccess = false;
            }
        }
        shutdownSuccess &= futureChannel.cancel(true);
        futureChannel = null;
        return shutdownSuccess;
    }

    @Override
    protected void initChannel(final SocketChannel ch) {
        final var txChain = dataBroker.createMergingTransactionChain();

        ch.pipeline()
            .addLast("frameDecoder", new MessageFrameDecoder())
            .addLast("idleStateHandler", new IdleStateHandler(
                keepaliveInterval.toNanos() * maxMissedKeepalives, 0, 0, TimeUnit.NANOSECONDS))
            .addLast("keepaliveHandler", new SinkKeepaliveHandler())
            .addLast("requestHandler", new SinkRequestHandler(TREE, txChain))
            .addLast("frameEncoder", MessageFrameEncoder.INSTANCE);

        txChain.addCallback(new FutureCallback<>() {
            @Override
            public void onSuccess(final Empty result) {
                LOG.info("Transaction chain for channel {} completed", ch);
            }

            @Override
            public void onFailure(final Throwable cause) {
                LOG.error("Transaction chain for channel {} failed", ch, cause);
                ch.close();
            }
        });
    }

    private synchronized void channelResolved(final ChannelFuture completedFuture,
            final ScheduledExecutorService group) {
        if (futureChannel != null && futureChannel.channel() == completedFuture.channel()) {
            if (completedFuture.isSuccess()) {
                final Channel ch = completedFuture.channel();
                LOG.info("Channel {} established", ch);
                ch.closeFuture().addListener((ChannelFutureListener) future -> channelClosed(future, group));
                ch.writeAndFlush(TREE_REQUEST);
            } else {
                LOG.info("Failed to connect to source {}, reconnecting in {}", sourceAddress,
                    reconnectDelay.getSeconds(), completedFuture.cause());
                group.schedule(() -> {
                    reconnect();
                }, reconnectDelay.toNanos(), TimeUnit.NANOSECONDS);
            }
        }
    }

    private synchronized void channelClosed(final ChannelFuture completedFuture, final ScheduledExecutorService group) {
        if (futureChannel != null && futureChannel.channel() == completedFuture.channel() && !closingInstance) {
            LOG.info("Channel {} lost connection to source {}, reconnecting in {}", completedFuture.channel(),
                sourceAddress, reconnectDelay.getSeconds());
            group.schedule(this::reconnect, reconnectDelay.toNanos(), TimeUnit.NANOSECONDS);
        }
    }

    private static ByteBuf requestTree(final DOMDataTreeIdentifier tree) throws IOException {
        final ByteBuf ret = Unpooled.buffer();

        try (ByteBufOutputStream stream = new ByteBufOutputStream(ret)) {
            stream.writeByte(Constants.MSG_SUBSCRIBE_REQ);
            try (NormalizedNodeDataOutput output = NormalizedNodeStreamVersion.current().newDataOutput(stream)) {
                tree.getDatastoreType().writeTo(output);
                output.writeYangInstanceIdentifier(tree.getRootIdentifier());
            }
        }

        return ret;
    }
}
