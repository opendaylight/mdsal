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
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.socket.ServerSocketChannel;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;

public abstract class AbstractBootstrapSupport implements AutoCloseable, BootstrapSupport {
    private final Class<? extends Channel> channelClass;
    private final Class<? extends ServerSocketChannel> serverChannelClass;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    AbstractBootstrapSupport(final Class<? extends Channel> channelClass,
            final Class<? extends ServerSocketChannel> serverChannelClass, final IoHandlerFactory ioHandlerFactory) {
        this.channelClass = requireNonNull(channelClass);
        this.serverChannelClass = requireNonNull(serverChannelClass);
        bossGroup = new MultiThreadIoEventLoopGroup(ioHandlerFactory);
        workerGroup = new MultiThreadIoEventLoopGroup(ioHandlerFactory);
    }

    public static @NonNull AbstractBootstrapSupport create() {
        if (Epoll.isAvailable()) {
            return new EpollBootstrapSupport();
        }
        return new NioBootstrapSupport();
    }

    @Override
    public final Bootstrap newBootstrap() {
        return new Bootstrap().group(workerGroup).channel(channelClass);
    }

    @Override
    public final ServerBootstrap newServerBootstrap() {
        return new ServerBootstrap().group(bossGroup, workerGroup).channel(serverChannelClass);
    }

    @Override
    public final void close() throws InterruptedException {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        bossGroup.awaitTermination(10, TimeUnit.SECONDS);
        workerGroup.awaitTermination(10, TimeUnit.SECONDS);
    }
}
