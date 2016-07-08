/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.binding.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;
import org.opendaylight.mdsal.eos.binding.api.Entity;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Unit tests for Entity.
 *
 * @author Thomas Pantelis
 */
public class EntityTest {
    static String ENTITY_TYPE1 = "type1";
    static String ENTITY_TYPE2 = "type2";
    static final InstanceIdentifier<TestDataObject1> ID1 = InstanceIdentifier.create(TestDataObject1.class);
    static final InstanceIdentifier<TestDataObject2> ID2 = InstanceIdentifier.create(TestDataObject2.class);

    @Test
    public void testHashCode() {
        Entity entity1 = new Entity(ENTITY_TYPE1, ID1);

        assertEquals("hashCode", entity1.hashCode(), new Entity(ENTITY_TYPE1, ID1).hashCode());
        assertNotEquals("hashCode", entity1.hashCode(), new Entity(ENTITY_TYPE2, ID2).hashCode());
    }

    @Test
    public void testEquals() {
        Entity entity1 = new Entity(ENTITY_TYPE1, ID1);

        assertEquals("Same", true, entity1.equals(entity1));
        assertEquals("Same", true, entity1.equals(new Entity(ENTITY_TYPE1, ID1)));
        assertEquals("Different entity type", false, entity1.equals(new Entity(ENTITY_TYPE2, ID1)));
        assertEquals("Different entity ID", false, entity1.equals(new Entity(ENTITY_TYPE1, ID2)));
        assertEquals("Different Object", false, entity1.equals(new Object()));
        assertEquals("Equals null", false, entity1.equals(null));
    }

    @Test
    public void testSerialization() {
        Entity entity = new Entity(ENTITY_TYPE1, ID1);

        Entity clone = SerializationUtils.clone(entity);

        assertEquals("getType", entity.getType(), clone.getType());
        assertEquals("getId", entity.getIdentifier(), clone.getIdentifier());
    }

    @Test
    public void testEntityNameConstructor() {
        Entity entity = new Entity(ENTITY_TYPE1, "foo");

        Identifier<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.
        mdsal.core.general.entity.rev150930.Entity> keyID = entity.getIdentifier().firstKeyOf(
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.core.general.entity.rev150930.Entity.class);
        assertNotNull("List key not found", keyID);
    }

    static class TestDataObject1 implements DataObject {
        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return null;
        }
    }

    static class TestDataObject2 implements DataObject {
        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return null;
        }
    }
}
