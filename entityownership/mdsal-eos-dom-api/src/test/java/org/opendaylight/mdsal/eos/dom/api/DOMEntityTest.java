/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.dom.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.opendaylight.mdsal.eos.dom.api.DOMEntity.ENTITY;
import java.util.List;
import java.util.Map.Entry;
import org.junit.Test;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * @author Thomas Pantelis
 */
public class DOMEntityTest {
    private static String ENTITY_TYPE1 = "type1";
    private static String ENTITY_TYPE2 = "type2";
    private static final YangInstanceIdentifier ID1 = YangInstanceIdentifier.of(QName.create(
            "test", "2015-11-24", "one"));
    private static final YangInstanceIdentifier ID2 = YangInstanceIdentifier.of(QName.create(
            "test", "2015-11-24", "two"));

    @Test
    public void testHashCode() {
        DOMEntity entity1 = new DOMEntity(ENTITY_TYPE1, ID1);

        assertEquals("hashCode", entity1.hashCode(), new DOMEntity(ENTITY_TYPE1, ID1).hashCode());
        assertNotEquals("hashCode", entity1.hashCode(), new DOMEntity(ENTITY_TYPE2, ID2).hashCode());
    }

    @Test
    public void testEquals() {
        DOMEntity entity1 = new DOMEntity(ENTITY_TYPE1, ID1);

        assertEquals("Same", true, entity1.equals(entity1));
        assertEquals("Same", true, entity1.equals(new DOMEntity(ENTITY_TYPE1, ID1)));
        assertEquals("Different entity type", false, entity1.equals(new DOMEntity(ENTITY_TYPE2, ID1)));
        assertEquals("Different entity ID", false, entity1.equals(new DOMEntity(ENTITY_TYPE1, ID2)));
        assertEquals("Different Object", false, entity1.equals(new Object()));
        assertEquals("Equals null", false, entity1.equals(null));
    }

    @Test
    public void testEntityNameConstructor() {
        DOMEntity entity = new DOMEntity(ENTITY_TYPE1, "foo");

        List<PathArgument> pathArgs = entity.getIdentifier().getPathArguments();
        assertEquals("pathArgs size", 2, pathArgs.size());
        assertEquals("First PathArgument node type", ENTITY, pathArgs.get(0).getNodeType());
        assertEquals("Second PathArgument node type", ENTITY, pathArgs.get(1).getNodeType());
        Entry<QName, Object> key = ((NodeIdentifierWithPredicates) pathArgs.get(1)).getKeyValues().entrySet()
                .iterator().next();
        assertEquals("Key node type", QName.create(ENTITY, "name"), key.getKey());
        assertEquals("Key value", "foo", key.getValue());
    }
}
