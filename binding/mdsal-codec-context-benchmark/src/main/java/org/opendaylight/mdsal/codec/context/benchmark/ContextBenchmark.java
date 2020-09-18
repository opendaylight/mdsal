/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.codec.context.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont100;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

public abstract class ContextBenchmark {
    protected List<Class<? extends DataObject>> contsClasses;
    protected List<Class<? extends DataObject>> listsClasses;
    protected List<Class<? extends DataObject>> rpcsClasses;
    protected ClassLoader loader = Cont100.class.getClassLoader();
    protected String packageName = Cont100.class.getPackageName();

    @SuppressWarnings("unchecked")
    protected void init(final int contsAmount, final int listsAmount, final int rpcsAmount) {
        this.contsClasses = new ArrayList<>(contsAmount);
        this.listsClasses = new ArrayList<>(listsAmount);
        this.rpcsClasses = new ArrayList<>(rpcsAmount);
        try {
            for (int i = 1; i <= contsAmount; i++) {
                contsClasses.add((Class<? extends DataObject>) loader.loadClass(packageName + ".Cont" + i));
            }
            for (int i = 1; i <= listsAmount; i++) {
                listsClasses.add((Class<? extends DataObject>) loader.loadClass(packageName + ".Lst" + i));
            }
            for (int i = 1; i <= rpcsAmount; i++) {
                rpcsClasses.add((Class<? extends DataObject>) loader
                        .loadClass(packageName + ".Rpc" + i + "Input"));
                rpcsClasses.add((Class<? extends DataObject>) loader
                        .loadClass(packageName + ".Rpc" + i + "Output"));
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    @Warmup(iterations = 7, timeUnit = TimeUnit.MICROSECONDS)
    @Measurement(iterations = 7, timeUnit = TimeUnit.MICROSECONDS)
    public void _600Containers() throws Exception {
        retrieveContainersInRange(1, 600);
    }

    @Benchmark
    @Warmup(iterations = 7, timeUnit = TimeUnit.MICROSECONDS)
    @Measurement(iterations = 7, timeUnit = TimeUnit.MICROSECONDS)
    public void _60Containers() throws Exception {
        retrieveContainersInRange(1, 60);
    }

    @Benchmark
    @Warmup(iterations = 7, timeUnit = TimeUnit.MICROSECONDS)
    @Measurement(iterations = 7, timeUnit = TimeUnit.MICROSECONDS)
    public void _600Lists() throws Exception {
        retrieveListsInRange(1, 600);
    }

    @Benchmark
    @Warmup(iterations = 7, timeUnit = TimeUnit.MICROSECONDS)
    @Measurement(iterations = 7, timeUnit = TimeUnit.MICROSECONDS)
    public void _60Lists() throws Exception {
        retrieveListsInRange(1, 60);
    }

    @Benchmark
    @Warmup(iterations = 7, timeUnit = TimeUnit.MICROSECONDS)
    @Measurement(iterations = 7, timeUnit = TimeUnit.MICROSECONDS)
    public void _600Rpcs() throws Exception {
        retrieveRpcsInRange(1, 600);
    }

    @Benchmark
    @Warmup(iterations = 7, timeUnit = TimeUnit.MICROSECONDS)
    @Measurement(iterations = 7, timeUnit = TimeUnit.MICROSECONDS)
    public void _60Rpcs() throws Exception {
        retrieveRpcsInRange(1, 60);
    }

    protected abstract void retrieveContainersInRange(int leftBound, int rightBound);

    protected abstract void retrieveListsInRange(int leftBound, int rightBound);

    protected abstract void retrieveRpcsInRange(int leftBound, int rightBound);
}
