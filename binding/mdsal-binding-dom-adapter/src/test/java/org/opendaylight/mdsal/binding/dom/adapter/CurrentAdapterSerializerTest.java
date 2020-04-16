/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map.Entry;
import org.junit.Test;
import org.opendaylight.binding.runtime.api.DefaultBindingRuntimeContext;
import org.opendaylight.binding.runtime.spi.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingRuntimeGenerator;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class CurrentAdapterSerializerTest {
    /**
     * Positive test.
     *
     * <p>
     * Test for yang with leaf of type int in container where data are created
     * with int value (acceptable data).
     *
     * @throws Exception
     *             - throw exception
     */
    @Test
    public void fromNormalizedNodeTest() throws Exception {
        final SchemaContext schemaCtx = YangParserTestUtils.parseYangResource("/test.yang");
        final NormalizedNode<?, ?> data = prepareData(schemaCtx, Uint16.valueOf(42));
        final Entry<InstanceIdentifier<?>, DataObject> fromNormalizedNode = fromNormalizedNode(data, schemaCtx);

        final DataObject value = fromNormalizedNode.getValue();
        assertNotNull(value);
        final Class<? extends DataObject> iface = value.implementedInterface();
        assertEquals("Cont", iface.getSimpleName());
        final Object[] objs = {};
        final Object invoked = iface.getDeclaredMethod("getVlanId").invoke(value, objs);
        final Field declaredField = invoked.getClass().getDeclaredField("_id");
        declaredField.setAccessible(true);
        final Object id = declaredField.get(invoked);
        final Field val = id.getClass().getDeclaredField("_value");
        val.setAccessible(true);
        assertEquals(Uint16.valueOf(42), val.get(id));
    }

    /**
     * Negative test.
     *
     * <p>
     * Test for yang with leaf of type int in container where data are created
     * with String value (non acceptable data - should failed with
     * {@link IllegalArgumentException})
     *
     * @throws Exception
     *             - throw exception
     */
    @Test
    public void fromNormalizedNodeWithAnotherInputDataTest() throws Exception {
        final SchemaContext schemaCtx = YangParserTestUtils.parseYangResource("/test.yang");
        final NormalizedNode<?, ?> data = prepareData(schemaCtx, "42");

        final Entry<InstanceIdentifier<?>, DataObject> fromNormalizedNode = fromNormalizedNode(data, schemaCtx);
        final DataObject value = fromNormalizedNode.getValue();
        assertNotNull(value);
        final Class<? extends DataObject> iface = value.implementedInterface();
        assertEquals("Cont", iface.getSimpleName());

        final Method getVlanId = iface.getDeclaredMethod("getVlanId");
        final InvocationTargetException ex = assertThrows(InvocationTargetException.class,
            () -> getVlanId.invoke(value));
        assertThat(ex.getCause(), instanceOf(IllegalArgumentException.class));
    }

    private static NormalizedNode<?, ?> prepareData(final SchemaContext schemaCtx, final Object value) {
        final DataSchemaNode dataChildByName =
                schemaCtx.getDataChildByName(QName.create("urn:test", "2017-01-01", "cont"));
        final DataSchemaNode leaf = ((ContainerSchemaNode) dataChildByName)
                .getDataChildByName(QName.create("urn:test", "2017-01-01", "vlan-id"));

        final DataContainerChild<?, ?> child = Builders.leafBuilder((LeafSchemaNode) leaf).withValue(value).build();
        final NormalizedNode<?, ?> data =
                Builders.containerBuilder((ContainerSchemaNode) dataChildByName).withChild(child).build();
        return data;
    }

    private static Entry<InstanceIdentifier<?>, DataObject> fromNormalizedNode(final NormalizedNode<?, ?> data,
            final SchemaContext schemaCtx) {
        final CurrentAdapterSerializer codec = new CurrentAdapterSerializer(new BindingCodecContext(
            DefaultBindingRuntimeContext.create(new DefaultBindingRuntimeGenerator().generateTypeMapping(schemaCtx),
                GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy())));

        final YangInstanceIdentifier path = YangInstanceIdentifier.create(NodeIdentifier.create(QName.create(
            "urn:test", "2017-01-01", "cont")));
        return codec.fromNormalizedNode(path, data);
    }
}
