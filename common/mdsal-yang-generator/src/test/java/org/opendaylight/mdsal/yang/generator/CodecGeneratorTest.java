/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yang.generator;

import static org.opendaylight.mdsal.yang.generator.LeafElem.stringLeaf;
import static org.opendaylight.mdsal.yang.generator.ModelWriter.MODULE_NAME;
import static org.opendaylight.mdsal.yang.generator.ModelWriter.NAMESPACE;
import static org.opendaylight.mdsal.yang.generator.ModelWriter.PREFIX;
import static org.opendaylight.mdsal.yang.generator.ModelWriter.REVISION;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;

public class CodecGeneratorTest {
    private static BindingRuntimeContext runtimeContext;

    @Before
    public void before() {
        runtimeContext = BindingRuntimeHelpers.createRuntimeContext();
        new BindingCodecContext(getRuntimeContext());
    }

    @Test
    public void test() {
        final ModuleElem module = new ModuleElem(MODULE_NAME, NAMESPACE, PREFIX, REVISION);
        for (int i = 6001; i <= 12000; i++) {
            module.with(new RpcElem("rpc" + i)
                    .with(new OperInputElem().with(stringLeaf("lf2")))
                    .with(new OperOutputElem().with(stringLeaf("lf3"))));
        }
        for (int i = 6001; i <= 12000; i++) {
            module.with(new ContainerElem("cont" + i).with(stringLeaf("lf1")));
        }
        for (int i = 6001; i <= 12000; i++) {
            module.with(new ListElem("lst" + i, "lf1").with(stringLeaf("lf1")).with(stringLeaf("lf2")));
        }
        final Path path = Path.of("target", "model");
        try {
            FileUtils.deleteDirectory(new File("target/model"));
            Files.deleteIfExists(path);
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
