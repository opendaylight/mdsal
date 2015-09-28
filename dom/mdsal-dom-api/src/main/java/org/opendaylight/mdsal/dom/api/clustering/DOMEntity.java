/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.clustering;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.common.api.clustering.GenericEntity;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Binding version of {@link GenericEntity}.
 *
 * @author Thomas Pantelis
 */
@Beta
public class DOMEntity extends GenericEntity<YangInstanceIdentifier> {
    private static final long serialVersionUID = 1L;

    // FIXME: needs update once the model is in
//    private static final QName ENTITY_QNAME =
//            org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.core.general.entity.rev150820.Entity.QNAME;
//    private static final QName ENTITY_NAME = QName.create(ENTITY_QNAME, "name");


    /** Constructs an instance.
     *
     * @param type the entity type
     * @param id the entity id.
     */
    public DOMEntity(@Nonnull final String type, @Nonnull final YangInstanceIdentifier id) {
        super(type, id);
    }

    /**
     * Construct an Entity with an with a name. The general-entity schema is used to construct the
     * YangInstanceIdentifier.
     *
     * @param type the type of the entity
     * @param entityName the name of the entity used to construct a general-entity YangInstanceIdentifier
     */
    // FIXME: needs update once the model is in
//    public DOMEntity(@Nonnull String type, @Nonnull String entityName) {
//        super(type, YangInstanceIdentifier.builder().node(ENTITY_QNAME).nodeWithKey(ENTITY_QNAME, ENTITY_NAME,
//                Preconditions.checkNotNull(entityName, "entityName should not be null")).build());
//    }
}
