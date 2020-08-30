/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.codec.context.benchmark;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.opendaylight.yang.gen.v1.bm.rev200829.Cont100;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ListViaUses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

public class CodecContextBenchmarkImpl extends AbstractCodecContextBenchmark {
    private static final Class<Cont100> CONT_100_CLASS = Cont100.class;
    private static final YangInstanceIdentifier LIST_ITEM_ID =
            YangInstanceIdentifier.builder(YangInstanceIdentifier.of(Top.QNAME))
                    .node(TopLevelList.QNAME)
                    .nodeWithKey(TopLevelList.QNAME, QName.create(TopLevelList.QNAME, "name"), "foo")
                    .build();
    private static final YangInstanceIdentifier.PathArgument AUGMENTATION_ID =
            YangInstanceIdentifier.AugmentationIdentifier.create(Set.of(ListViaUses.QNAME,
                    QName.create(ListViaUses.QNAME, "container-with-uses")));

    private static final SchemaNodeIdentifier.Absolute SCHEMA_AUG_LIST_ID = SchemaNodeIdentifier.Absolute.of(Top.QNAME,
            TopLevelList.QNAME,
            QName.create(ListViaUses.QNAME, "list-via-uses"));

    @Benchmark
    @Warmup(iterations = 7, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 7, timeUnit = TimeUnit.MILLISECONDS)
    public void container200list200_cont100th() throws Exception {
        root.streamChild(Cont100.class);
    }
}
