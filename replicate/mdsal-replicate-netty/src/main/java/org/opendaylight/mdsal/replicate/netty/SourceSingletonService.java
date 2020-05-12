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
import com.google.common.util.concurrent.SettableFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SourceSingletonService extends ChannelInitializer<SocketChannel> implements ClusterSingletonService {
    private static final Logger LOG = LoggerFactory.getLogger(SourceSingletonService.class);
    private static final ServiceGroupIdentifier SGID =
            ServiceGroupIdentifier.create(SourceSingletonService.class.getName());

    private final BootstrapSupport bootstrapSupport;
    private final DOMDataTreeChangeService dtcs;
    private final int listenPort;

    @GuardedBy("this")
    private final Collection<SocketChannel> children = new HashSet<>();
    @GuardedBy("this")
    private Channel serverChannel;

    SourceSingletonService(final BootstrapSupport bootstrapSupport, final DOMDataTreeChangeService dtcs,
            final int listenPort) {
        this.bootstrapSupport = requireNonNull(bootstrapSupport);
        this.dtcs = requireNonNull(dtcs);
        this.listenPort = listenPort;
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
            serverChannel = bootstrapSupport.newServerBootstrap().bind(listenPort).sync().channel();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Failed to bind port " + listenPort, e);
        }
    }

    @Override
    public synchronized ListenableFuture<?> closeServiceInstance() {
        final List<ListenableFuture<Void>> futures = new ArrayList<>();

        // Close server channel
        futures.add(closeChannel(serverChannel));
        serverChannel = null;

        // Close all child channels
        for (SocketChannel channel : children) {
            futures.add(closeChannel(channel));
        }
        children.clear();

        return Futures.nonCancellationPropagating(Futures.successfulAsList(futures));
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
            .addLast("messageHandler", new SourceChannelHandler(dtcs, ch))
            .addLast("frameEncoder", MessageFrameEncoder.instance());
        children.add(ch);

        LOG.debug("Channel {} established", ch);
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
