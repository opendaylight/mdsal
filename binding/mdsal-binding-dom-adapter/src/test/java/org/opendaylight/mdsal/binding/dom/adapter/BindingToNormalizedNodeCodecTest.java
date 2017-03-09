/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;
import javassist.ClassPool;

public class BindingToNormalizedNodeCodecTest {

    @Test
    public void fromNormalizedNodeTest() throws Exception {
        final StreamWriterGenerator serializerGenerator =
                new StreamWriterGenerator(JavassistUtils.forClassPool(ClassPool.getDefault()));
        final BindingNormalizedNodeCodecRegistry codecRegistry =
                new BindingNormalizedNodeCodecRegistry(serializerGenerator);
        final GeneratedClassLoadingStrategy classLoadingStrategy =
                GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy();
        final SchemaContext schemaCtx = loadSchemaContext("/");
        final BindingRuntimeContext ctx = BindingRuntimeContext.create(classLoadingStrategy, schemaCtx);
        codecRegistry.onBindingRuntimeContextUpdated(ctx);
        final BindingToNormalizedNodeCodec codec =
                new BindingToNormalizedNodeCodec(classLoadingStrategy, codecRegistry);

        final DataSchemaNode dataChildByName =
                schemaCtx.getDataChildByName(QName.create("urn:test", "2017-01-01", "cont"));
        final DataSchemaNode leaf = ((ContainerSchemaNode) dataChildByName)
                .getDataChildByName(QName.create("urn:test", "2017-01-01", "vlan-id"));

        final DataContainerChild<?, ?> child = Builders.leafBuilder((LeafSchemaNode) leaf).withValue(2420).build();
        final ContainerNode data =
                Builders.containerBuilder((ContainerSchemaNode) dataChildByName).withChild(child).build();


        final List<PathArgument> pathArgs = new ArrayList<>();
        pathArgs.add(NodeIdentifier.create(QName.create("urn:test", "2017-01-01", "cont")));

        final YangInstanceIdentifier path = YangInstanceIdentifier.create(pathArgs);
        final Entry<InstanceIdentifier<?>, DataObject> fromNormalizedNode = codec.fromNormalizedNode(path, data);

        final DataObject value = fromNormalizedNode.getValue();
        Assert.assertNotNull(value);
        Assert.assertEquals("Cont", value.getImplementedInterface().getSimpleName());
        final Object objs[] = {};
        final Object invoked = value.getImplementedInterface().getDeclaredMethods()[0].invoke(value, objs);
        final Field declaredField = invoked.getClass().getDeclaredField("_id");
        declaredField.setAccessible(true);
        final Object id = declaredField.get(invoked);
        final Field val = id.getClass().getDeclaredField("_value");
        val.setAccessible(true);
        Assert.assertEquals(2420, val.get(id));
    }

    public static SchemaContext loadSchemaContext(final String... yangPath)
            throws FileNotFoundException, ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();

        for (int i = 0; i < yangPath.length; i++) {
            final String path = yangPath[i];
            final String pathToFile = BindingToNormalizedNodeCodecTest.class.getResource(path).getPath();
            final File testDir = new File(pathToFile);
            final String[] fileList = testDir.list();
            if (fileList == null) {
                throw new FileNotFoundException(pathToFile);
            }
            for (int j = 0; j < fileList.length; j++) {
                final String fileName = fileList[j];
                final File file = new File(testDir, fileName);
                if (file.isDirectory() == false) {
                    reactor.addSource(new YangStatementSourceImpl(new NamedFileInputStream(file, file.getPath())));
                }
            }
        }
        return reactor.buildEffective();
    }
}
