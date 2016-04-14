/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.common.api.clustering.util;

import javax.annotation.Nonnull;
import org.opendaylight.mdsal.common.api.clustering.GenericEntity;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Test util class.
 */
public class TestEntity extends GenericEntity<TestInstanceIdentifier> {
    private static final long serialVersionUID = 1L;

    static final QName ENTITY = QName
            .create("urn:opendaylight:params:xml:ns:yang:mdsal:core:test-entity", "2016-06-06", "entity").intern();
    static final QName ENTITY_NAME = QName.create(ENTITY, "name").intern();

    /** Constructs an instance.
     *
     * @param type the entity type
     * @param id the entity id.
     */
    public TestEntity(@Nonnull final String type, @Nonnull final TestInstanceIdentifier id) {
        super(type, id);
    }

    /**
     * Construct an Entity with an with a name. The general-entity schema is used to construct the
     * {@link TestInstanceIdentifier}.
     *
     * @param type the type of the entity
     * @param entityName the name of the entity used to construct a general-entity YangInstanceIdentifier
     */
    public TestEntity(@Nonnull final String type, @Nonnull final String entityName) {
        super(type, new TestInstanceIdentifier(entityName));
    }
}
