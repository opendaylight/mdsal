/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.codec.context.benchmark;

import java.util.concurrent.TimeUnit;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.mdsal.binding.dom.codec.impl.SchemaRootCodecContext;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
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

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(1)
@SuppressWarnings({"checkstyle:typeName"})
public class _3kLists3kConts3kRPCsSeparate extends _3kLists3kConts3kRPCs {
    private static BindingRuntimeContext runtimeContext;
    protected BindingCodecContext codecContext;
    protected SchemaRootCodecContext<?> root;

    @Setup(Level.Trial)
    @SuppressWarnings("unchecked")
    public void setup() throws ClassNotFoundException {
        runtimeContext = BindingRuntimeHelpers.createRuntimeContext();
        this.codecContext = new BindingCodecContext(runtimeContext);
        this.root = codecContext.getRoot();

        for (int i = 1; i <= 3000; i++) {
            this.root.streamChild((Class<? extends DataObject>)loader.loadClass(packageName + ".Cont" + i));
            this.root.streamChild((Class<? extends DataObject>)loader.loadClass(packageName + ".Lst" + i));
            this.root.getRpc((Class<? extends DataObject>)loader.loadClass(packageName + ".Rpc" + i + "Input"));
            this.root.getRpc((Class<? extends DataObject>)loader.loadClass(packageName + ".Rpc" + i + "Output"));
        }
    }

    @Override
    protected void retrieveContainersInRange(int leftBound, int rightBound) {
        for (int i = leftBound; i <= rightBound; i++) {
            root.streamChild(contsClasses.get(i));
        }
    }

    @Override
    protected void retrieveListsInRange(int leftBound, int rightBound) {
        for (int i = leftBound; i <= rightBound; i++) {
            root.streamChild(listsClasses.get(i));
        }
    }


    @Override
    protected void retrieveRpcsInRange(int leftBound, int rightBound) {
        for (int i = leftBound; i <= rightBound; i++) {
            root.streamChild(rpcsClasses.get(i));
        }
    }

    @TearDown
    public void tearDown() {
        runtimeContext = null;
        codecContext = null;
        root = null;
    }
}
