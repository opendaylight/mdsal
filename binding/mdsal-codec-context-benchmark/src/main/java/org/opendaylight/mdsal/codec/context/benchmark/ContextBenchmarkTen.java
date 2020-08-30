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
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11010;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11011;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11012;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11013;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11014;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11015;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11016;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11017;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11018;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11019;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11020;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11021;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11022;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11023;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11024;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11025;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11026;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11027;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11028;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11029;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11030;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont11031;
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
    private static final Class<Cont11010> CONT_11010_CLASS = Cont11010.class;
    private static final Class<Cont11011> CONT_11011_CLASS = Cont11011.class;
    private static final Class<Cont11012> CONT_11012_CLASS = Cont11012.class;
    private static final Class<Cont11013> CONT_11013_CLASS = Cont11013.class;
    private static final Class<Cont11014> CONT_11014_CLASS = Cont11014.class;
    private static final Class<Cont11015> CONT_11015_CLASS = Cont11015.class;
    private static final Class<Cont11016> CONT_11016_CLASS = Cont11016.class;
    private static final Class<Cont11017> CONT_11017_CLASS = Cont11017.class;
    private static final Class<Cont11018> CONT_11018_CLASS = Cont11018.class;
    private static final Class<Cont11019> CONT_11019_CLASS = Cont11019.class;
    private static final Class<Cont11020> CONT_11020_CLASS = Cont11020.class;
    private static final Class<Cont11021> CONT_11021_CLASS = Cont11021.class;
    private static final Class<Cont11022> CONT_11022_CLASS = Cont11022.class;
    private static final Class<Cont11023> CONT_11023_CLASS = Cont11023.class;
    private static final Class<Cont11024> CONT_11024_CLASS = Cont11024.class;
    private static final Class<Cont11025> CONT_11025_CLASS = Cont11025.class;
    private static final Class<Cont11026> CONT_11026_CLASS = Cont11026.class;
    private static final Class<Cont11027> CONT_11027_CLASS = Cont11027.class;
    private static final Class<Cont11028> CONT_11028_CLASS = Cont11028.class;
    private static final Class<Cont11029> CONT_11029_CLASS = Cont11029.class;
    private static final Class<Cont11030> CONT_11030_CLASS = Cont11030.class;
    private static final Class<Cont11031> CONT_11031_CLASS = Cont11031.class;

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
        root.streamChild(CONT_11010_CLASS);
        root.streamChild(CONT_11011_CLASS);
        root.streamChild(CONT_11012_CLASS);
        root.streamChild(CONT_11013_CLASS);
        root.streamChild(CONT_11014_CLASS);
        root.streamChild(CONT_11015_CLASS);
        root.streamChild(CONT_11016_CLASS);
        root.streamChild(CONT_11017_CLASS);
        root.streamChild(CONT_11018_CLASS);
        root.streamChild(CONT_11019_CLASS);
        root.streamChild(CONT_11020_CLASS);
        root.streamChild(CONT_11021_CLASS);
        root.streamChild(CONT_11022_CLASS);
        root.streamChild(CONT_11023_CLASS);
        root.streamChild(CONT_11024_CLASS);
        root.streamChild(CONT_11025_CLASS);
        root.streamChild(CONT_11026_CLASS);
        root.streamChild(CONT_11027_CLASS);
        root.streamChild(CONT_11028_CLASS);
        root.streamChild(CONT_11029_CLASS);
        root.streamChild(CONT_11030_CLASS);
        root.streamChild(CONT_11031_CLASS);
    }
}
