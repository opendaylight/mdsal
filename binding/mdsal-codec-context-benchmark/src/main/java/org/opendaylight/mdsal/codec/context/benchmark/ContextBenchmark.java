/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.codec.context.benchmark;

import org.opendaylight.yang.gen.v1.bm.rev200829.Cont100;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class ContextBenchmark {
    protected final List<Class<? extends DataObject>> classes = new ArrayList<>(5900);

    {
        final Class<Cont100> cont100Class = Cont100.class;
        final ClassLoader loader = cont100Class.getClassLoader();
        final String packageName = cont100Class.getPackageName();
        for (int i = 1; i <= 5900; i++) {
            try {
                classes.add((Class<? extends DataObject>)loader.loadClass(packageName + ".Cont" + i));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 7, timeUnit = TimeUnit.MICROSECONDS)
    @Measurement(iterations = 7, timeUnit = TimeUnit.MICROSECONDS)
    public void _6kLists6kContainers6kRPCs_600Containers() throws Exception {
        retrieveContainersInRange(1, 600);
    }

    @Benchmark
    @Warmup(iterations = 7, timeUnit = TimeUnit.MICROSECONDS)
    @Measurement(iterations = 7, timeUnit = TimeUnit.MICROSECONDS)
    public void _6kLists6kContainers6kRPCs_60Containers() throws Exception {
        retrieveContainersInRange(50, 110);
    }

    protected abstract void retrieveContainersInRange(int leftBound, int rightBound);
}
