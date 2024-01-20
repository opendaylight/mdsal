/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.opendaylight.mdsal.dom.api.DOMDataBroker.DataTreeChangeExtension;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.api.ServiceGroupIdentifier;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeStreamVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cluster Singleton Service handler for delta stream source. Responsible for starting/stopping the delta stream source
 * for a particular port.
 */
final class SourceSingletonService extends ChannelInitializer<SocketChannel> implements ClusterSingletonService {
    private static final Logger LOG = LoggerFactory.getLogger(SourceSingletonService.class);
    private static final ServiceGroupIdentifier SGID =
        new ServiceGroupIdentifier(SourceSingletonService.class.getName());

    private final BootstrapSupport bootstrapSupport;
    private final DataTreeChangeExtension dtcs;
    private final int listenPort;

    @GuardedBy("this")
    private final Collection<SocketChannel> children = new HashSet<>();
    private final Duration keepaliveInterval;
    private final int maxMissedKeepalives;
    @GuardedBy("this")
    private Channel serverChannel;

    SourceSingletonService(final BootstrapSupport bootstrapSupport, final DataTreeChangeExtension dtcs,
            final int listenPort, final Duration keepaliveInterval, final int maxMissedKeepalives) {
        this.bootstrapSupport = requireNonNull(bootstrapSupport);
        this.dtcs = requireNonNull(dtcs);
        this.listenPort = listenPort;
        this.keepaliveInterval = requireNonNull(keepaliveInterval);
        this.maxMissedKeepalives = maxMissedKeepalives;
        LOG.info("Replication source on port {} waiting for cluster-wide mastership", listenPort);
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return SGID;
    }

    @Override
    public synchronized void instantiateServiceInstance() {
        final ChannelFuture future = bootstrapSupport.newServerBootstrap()
                .option(ChannelOption.SO_BACKLOG, 3)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(this)
                .bind(listenPort);

        try {
            future.sync();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Failed to bind port " + listenPort, e);
        }

        serverChannel = future.channel();
        LOG.info("Replication source started on port {}", listenPort);
    }

    @Override
    public synchronized ListenableFuture<?> closeServiceInstance() {
        LOG.info("Replication source on port {} shutting down", listenPort);

        final List<ListenableFuture<Void>> futures = new ArrayList<>();

        // Close server channel
        futures.add(closeChannel(serverChannel));
        serverChannel = null;

        // Close all child channels
        for (SocketChannel channel : children) {
            futures.add(closeChannel(channel));
        }
        children.clear();

        final ListenableFuture<?> ret = Futures.nonCancellationPropagating(Futures.successfulAsList(futures));
        ret.addListener(() -> {
            LOG.info("Replication source on port {} shut down", listenPort);
        }, MoreExecutors.directExecutor());
        return ret;
    }

    @Override
    public synchronized void initChannel(final SocketChannel ch) {
        if (serverChannel == null) {
            LOG.debug("Channel {} established while shutting down, closing it", ch);
            ch.close();
            return;
        }

        ch.pipeline()
            .addLast("frameDecoder", new MessageFrameDecoder())
            .addLast("idleStateHandler", new IdleStateHandler(0, keepaliveInterval.toNanos(), 0, TimeUnit.NANOSECONDS))
            .addLast("keepaliveHandler", new SourceKeepaliveHandler(maxMissedKeepalives))
            .addLast("requestHandler", new SourceRequestHandler(dtcs))
            // Output, in reverse order
            .addLast("frameEncoder", MessageFrameEncoder.INSTANCE)
            .addLast("dtclHandler", new DeltaEncoder(NormalizedNodeStreamVersion.current()));
        children.add(ch);

        LOG.info("Channel {} established", ch);
    }

    private static ListenableFuture<Void> closeChannel(final Channel ch) {
        final SettableFuture<Void> ret = SettableFuture.create();
        ch.closeFuture().addListener(chf -> {
            final Throwable cause = chf.cause();
            if (cause != null) {
                ret.setException(cause);
            } else {
                ret.set(null);
            }
        });

        ch.close();
        return ret;
    }
}
