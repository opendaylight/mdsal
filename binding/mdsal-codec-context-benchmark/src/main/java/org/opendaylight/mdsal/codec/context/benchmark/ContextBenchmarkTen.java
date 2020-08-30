/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.codec.context.benchmark;

import java.util.concurrent.TimeUnit;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11000;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11001;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11002;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11003;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11004;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11005;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11006;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11007;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11008;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11009;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

public class ContextBenchmarkTen extends ContextBenchmarkBaseTen {
    private static final Class<Cont11000> CONT_11000_CLASS = Cont11000.class;
    private static final Class<Cont11001> CONT_11001_CLASS = Cont11001.class;
    private static final Class<Cont11002> CONT_11002_CLASS = Cont11002.class;
    private static final Class<Cont11003> CONT_11003_CLASS = Cont11003.class;
    private static final Class<Cont11004> CONT_11004_CLASS = Cont11004.class;
    private static final Class<Cont11005> CONT_11005_CLASS = Cont11005.class;
    private static final Class<Cont11006> CONT_11006_CLASS = Cont11006.class;
    private static final Class<Cont11007> CONT_11007_CLASS = Cont11007.class;
    private static final Class<Cont11008> CONT_11008_CLASS = Cont11008.class;
    private static final Class<Cont11009> CONT_11009_CLASS = Cont11009.class;

    @Benchmark
    @Warmup(iterations = 7, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 7, timeUnit = TimeUnit.MILLISECONDS)
    public void container200list200_cont100th() throws Exception {
        root.streamChild(CONT_11000_CLASS);
        root.streamChild(CONT_11001_CLASS);
        root.streamChild(CONT_11002_CLASS);
        root.streamChild(CONT_11003_CLASS);
        root.streamChild(CONT_11004_CLASS);
        root.streamChild(CONT_11005_CLASS);
        root.streamChild(CONT_11006_CLASS);
        root.streamChild(CONT_11007_CLASS);
        root.streamChild(CONT_11008_CLASS);
        root.streamChild(CONT_11009_CLASS);
    }
}
