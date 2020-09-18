/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.codec.context.benchmark;

import org.opendaylight.mdsal.binding.dom.codec.merge.impl.BindingCodecContext;
import org.opendaylight.mdsal.binding.dom.codec.merge.impl.SchemaRootCodecContext;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont100;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.openjdk.jmh.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(1)
public class ContextBenchmarkMerge extends ContextBenchmark {
    private static final Logger LOG = LoggerFactory.getLogger(ContextBenchmarkSeparate.class);

    private static BindingRuntimeContext runtimeContext;
    protected BindingCodecContext codecContext;
    protected SchemaRootCodecContext<?> root;

    @Setup(Level.Trial)
    public void setup() throws ClassNotFoundException {
        runtimeContext = BindingRuntimeHelpers.createRuntimeContext();
        this.codecContext = new BindingCodecContext(getRuntimeContext());
        this.root = codecContext.getRoot();

        for (int i = 1; i <= 6000; i++) {
            final Class<Cont100> cont100Class = Cont100.class;
            final ClassLoader loader = cont100Class.getClassLoader();
            final String packageName = cont100Class.getPackageName();

            final SchemaRootCodecContext<?> rootCodecContext = this.root;
            rootCodecContext
                    .streamChild((Class<? extends DataObject>)loader.loadClass(packageName + ".Cont" + i));
            rootCodecContext.streamChild((Class<? extends DataObject>)loader.loadClass(packageName + ".Lst" + i));
            rootCodecContext
                    .getRpc((Class<? extends DataObject>)loader.loadClass(packageName + ".Rpc" + i + "Input"));
            rootCodecContext
                    .getRpc((Class<? extends DataObject>)loader.loadClass(packageName + ".Rpc" + i + "Output"));
        }
    }

    public static BindingRuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

    @TearDown
    public void tearDown() {
        codecContext = null;
        runtimeContext = null;
    }

    @Override
    protected void retrieveContainersInRange(int leftBound, int rightBound) {
        for (int i = leftBound; i <= rightBound; i++) {
            root.streamChild(classes.get(i));
        }
    }
}
