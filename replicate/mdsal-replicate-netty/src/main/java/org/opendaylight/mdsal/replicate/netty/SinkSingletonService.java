/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.util.concurrent.Future;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeDataOutput;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeStreamVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SinkSingletonService implements ClusterSingletonService {
    private static final Logger LOG = LoggerFactory.getLogger(SinkSingletonService.class);
    private static final ServiceGroupIdentifier SGID =
            ServiceGroupIdentifier.create(SinkSingletonService.class.getName());
    // TODO: allow different trees?
    private static final DOMDataTreeIdentifier TREE = new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION,
        YangInstanceIdentifier.empty());
    private static final ByteBuf TREE_REQUEST;

    static {
        try {
            TREE_REQUEST = requestTree(TREE);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final BootstrapSupport bootstrapSupport;
    private final DOMDataBroker dataBroker;
    private final InetSocketAddress sourceAddress;
    private final Duration reconnectDelay;

    @GuardedBy("this")
    private ChannelFuture futureChannel;

    SinkSingletonService(final BootstrapSupport bootstrapSupport, final DOMDataBroker dataBroker,
            final InetSocketAddress sourceAddress, final Duration reconnectDelay) {
        this.bootstrapSupport = requireNonNull(bootstrapSupport);
        this.dataBroker = requireNonNull(dataBroker);
        this.sourceAddress = requireNonNull(sourceAddress);
        this.reconnectDelay = requireNonNull(reconnectDelay);
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return SGID;
    }

    @Override
    public synchronized void instantiateServiceInstance() {
        LOG.info("Replication sink started with source {}", sourceAddress);

        final Bootstrap bs = bootstrapSupport.newBootstrap();
        final ScheduledExecutorService group = bs.config().group();

        futureChannel = bs
                .option(ChannelOption.SO_KEEPALIVE, true)
                .connect(sourceAddress, null);

        futureChannel.addListener(compl -> channelResolved(compl, group));
    }

    @Override
    public synchronized ListenableFuture<?> closeServiceInstance() {
        // TODO Auto-generated method stub
        return null;
    }

    private synchronized void channelResolved(final Future<?> completedFuture, final ScheduledExecutorService group) {
        if (completedFuture != futureChannel) {
            // Future changed, this callback is irrelevant
            return;
        }

        final Channel channel = futureChannel.channel();
        channel.pipeline()
            .addLast("frameDecoder", new MessageFrameDecoder())
            .addLast("requestHandler", new SinkRequestHandler(TREE, dataBroker.createMergingTransactionChain(
                new SinkTransactionChainListener(channel))))
            .addLast("frameEncoder", MessageFrameEncoder.instance());

        channel.writeAndFlush(TREE_REQUEST);
    }

    private static ByteBuf requestTree(final DOMDataTreeIdentifier tree) throws IOException {
        final ByteBuf ret = Unpooled.buffer();

        try (ByteBufOutputStream stream = new ByteBufOutputStream(ret)) {
            try (NormalizedNodeDataOutput output = NormalizedNodeStreamVersion.current().newDataOutput(stream)) {
                tree.getDatastoreType().writeTo(output);
                output.writeYangInstanceIdentifier(tree.getRootIdentifier());
            }
        }

        return ret;
    }
}
