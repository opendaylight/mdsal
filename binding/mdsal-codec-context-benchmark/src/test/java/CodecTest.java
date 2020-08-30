/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import static org.opendaylight.mdsal.codec.context.benchmark.LeafElem.stringLeaf;
import static org.opendaylight.mdsal.codec.context.benchmark.ModelWriter.MODULE_NAME;
import static org.opendaylight.mdsal.codec.context.benchmark.ModelWriter.NAMESPACE;
import static org.opendaylight.mdsal.codec.context.benchmark.ModelWriter.PREFIX;
import static org.opendaylight.mdsal.codec.context.benchmark.ModelWriter.REVISION;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.mdsal.codec.context.benchmark.ContainerElem;
import org.opendaylight.mdsal.codec.context.benchmark.ListElem;
import org.opendaylight.mdsal.codec.context.benchmark.ModuleElem;
import org.opendaylight.mdsal.codec.context.benchmark.RpcElem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ListViaUses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class CodecTest {
    private static BindingRuntimeContext runtimeContext;
    private BindingCodecContext codecContext;

    private static final YangInstanceIdentifier LIST_ITEM_ID =
            YangInstanceIdentifier.builder(YangInstanceIdentifier.of(Top.QNAME))
                    .node(TopLevelList.QNAME)
                    .nodeWithKey(TopLevelList.QNAME, QName.create(TopLevelList.QNAME, "name"), "foo")
                    .build();
    private static final YangInstanceIdentifier.PathArgument AUGMENTATION_ID =
            YangInstanceIdentifier.AugmentationIdentifier.create(Set.of(ListViaUses.QNAME,
                    QName.create(ListViaUses.QNAME, "container-with-uses")));

    @Before
    public void before() {
        runtimeContext = BindingRuntimeHelpers.createRuntimeContext();
        this.codecContext = new BindingCodecContext(getRuntimeContext());
    }

    @Test
    public void test() {
        final ModuleElem module = new ModuleElem(MODULE_NAME, NAMESPACE, PREFIX, REVISION);
        for (int i = 1; i <= 3000; i++) {
            module.with(new RpcElem("rpc" + i));
        }
        for (int i = 0; i <= 3000; i++) {
            module.with(new ContainerElem("cont" + i).with(stringLeaf("lf1")));
        }
        for (int i = 0; i <= 3000; i++) {
            module.with(new ListElem("lst" + i).with(stringLeaf("lf1")));
        }
        final Path path = Path.of("target", "model");
        try {
            Files.createDirectory(path);
            Files.writeString(Path.of("target", "model", "a.yang"), module.build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BindingRuntimeContext getRuntimeContext() {
        return runtimeContext;
    }
}
