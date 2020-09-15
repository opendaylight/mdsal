/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.codec.context.benchmark;

import java.util.concurrent.TimeUnit;
import org.opendaylight.mdsal.binding.dom.codec.merge.impl.BindingCodecContext;
import org.opendaylight.mdsal.binding.dom.codec.merge.impl.SchemaRootCodecContext;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11000;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public abstract class MergeContexstBenchmarkBase {
    private static final Logger LOG = LoggerFactory.getLogger(ContextBenchmarkBaseTen.class);

    private static BindingRuntimeContext runtimeContext;
    protected BindingCodecContext codecContext;
    protected SchemaRootCodecContext<?> root;

    @Setup(Level.Trial)
    public void setup() throws ClassNotFoundException {
        runtimeContext = BindingRuntimeHelpers.createRuntimeContext();
        this.codecContext = new BindingCodecContext(getRuntimeContext());
        this.root = (SchemaRootCodecContext<?>) codecContext.getRoot();

        for (int i = 6001; i <= 12000; i++) {
            final Class<Cont11000> cont100Class = Cont11000.class;
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
}

