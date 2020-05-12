/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static java.util.Objects.requireNonNull;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;

/**
 * @author Robert Varga
 *
 */
public abstract class NettyBootstrapSupport implements AutoCloseable {
    private static final class NioBootstrapSupport extends NettyBootstrapSupport {
        NioBootstrapSupport() {
            super(NioSocketChannel.class, NioServerSocketChannel.class, new NioEventLoopGroup(),
                new NioEventLoopGroup());
        }
    }

    private static final class EpollBootstrapSupport extends NettyBootstrapSupport {
        EpollBootstrapSupport() {
            super(EpollSocketChannel.class, EpollServerSocketChannel.class, new EpollEventLoopGroup(),
                new EpollEventLoopGroup());
        }
    }

    private final Class<? extends Channel> channelClass;
    private final Class<? extends ServerSocketChannel> serverChannelClass;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    NettyBootstrapSupport(final Class<? extends Channel> channelClass,
            final Class<? extends ServerSocketChannel> serverChannelClass, final EventLoopGroup bossGroup,
            final EventLoopGroup workerGroup) {
        this.channelClass = requireNonNull(channelClass);
        this.serverChannelClass = requireNonNull(serverChannelClass);
        this.bossGroup = requireNonNull(bossGroup);
        this.workerGroup = requireNonNull(workerGroup);
    }

    public static NettyBootstrapSupport create() {
        return Epoll.isAvailable() ? new EpollBootstrapSupport() : new NioBootstrapSupport();
    }

    @Override
    public final void close() throws InterruptedException {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        bossGroup.awaitTermination(10, TimeUnit.SECONDS);
        workerGroup.awaitTermination(10, TimeUnit.SECONDS);
    }

    final @NonNull Bootstrap newBootstrap() {
        return new Bootstrap().group(workerGroup).channel(channelClass);
    }

    final @NonNull ServerBootstrap newServerBootstrap() {
        return new ServerBootstrap().group(bossGroup, workerGroup).channel(serverChannelClass);
    }
}
