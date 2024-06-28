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
import java.util.ServiceLoader;
import org.junit.Test;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecFactory;
import org.opendaylight.yangtools.binding.generator.impl.DefaultBindingRuntimeGenerator;
import org.opendaylight.yangtools.binding.runtime.api.DefaultBindingRuntimeContext;
import org.opendaylight.yangtools.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
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
        final var schemaCtx = YangParserTestUtils.parseYangResource("/test.yang");
        final var data = prepareData(schemaCtx, Uint16.valueOf(42));
        final var fromNormalizedNode = fromNormalizedNode(data, schemaCtx);

        final var value = fromNormalizedNode.getValue();
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
        final var schemaCtx = YangParserTestUtils.parseYangResource("/test.yang");
        final var data = prepareData(schemaCtx, "42");

        final var fromNormalizedNode = fromNormalizedNode(data, schemaCtx);
        final var value = fromNormalizedNode.getValue();
        assertNotNull(value);
        final Class<? extends DataObject> iface = value.implementedInterface();
        assertEquals("Cont", iface.getSimpleName());

        final Method getVlanId = iface.getDeclaredMethod("getVlanId");
        final InvocationTargetException ex = assertThrows(InvocationTargetException.class,
            () -> getVlanId.invoke(value));
        assertThat(ex.getCause(), instanceOf(IllegalArgumentException.class));
    }

    private static ContainerNode prepareData(final EffectiveModelContext schemaCtx, final Object value) {
        return ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(QName.create("urn:test", "2017-01-01", "cont")))
            .withChild(ImmutableNodes.leafNode(QName.create("urn:test", "2017-01-01", "vlan-id"), value))
            .build();
    }

    private static Entry<DataObjectReference<?>, DataObject> fromNormalizedNode(final NormalizedNode data,
            final EffectiveModelContext schemaCtx) {
        final var codec = new CurrentAdapterSerializer(
            ServiceLoader.load(BindingDOMCodecFactory.class).findFirst().orElseThrow()
                .createBindingDOMCodec(new DefaultBindingRuntimeContext(
                    new DefaultBindingRuntimeGenerator().generateTypeMapping(schemaCtx),
                    TestingModuleInfoSnapshot.INSTANCE)));

        final YangInstanceIdentifier path = YangInstanceIdentifier.of(NodeIdentifier.create(QName.create(
            "urn:test", "2017-01-01", "cont")));
        return codec.fromNormalizedNode(path, data);
    }

    private static final class TestingModuleInfoSnapshot implements ModuleInfoSnapshot {
        static final TestingModuleInfoSnapshot INSTANCE = new TestingModuleInfoSnapshot();

        private TestingModuleInfoSnapshot() {
            // Hidden on purpose
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Class<T> loadClass(final String fullyQualifiedName) throws ClassNotFoundException {
            return (Class<T>) Class.forName(fullyQualifiedName);
        }

        @Override
        public YangTextSource yangTextSource(final SourceIdentifier arg0) {
            return null;
        }

        @Override
        public YangTextSource getYangTextSource(final SourceIdentifier sourceId) throws MissingSchemaSourceException {
            throw new MissingSchemaSourceException(sourceId, "no sources");
        }

        @Override
        public EffectiveModelContext modelContext() {
            throw new UnsupportedOperationException();
        }

    }
}
