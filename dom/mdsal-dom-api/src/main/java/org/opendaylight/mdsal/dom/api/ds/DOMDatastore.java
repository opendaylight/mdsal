/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.ds;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Conventional;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Datastore;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Dynamic;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * A baseline datastore definition. Use {@link DOMConfigurationDatastore} and {@link DOMOperationalDatastore} directly
 * to acquire instances.
 */
@NonNullByDefault
public sealed interface DOMDatastore extends DOMService<DOMDatastore, DOMDatastore.Extension>
        permits DOMLogicalDatastore {
    /**
     * Marker interface for extensions of {@link DOMDatastore}.
     */
    interface Extension extends DOMService.Extension<DOMDatastore, Extension> {
        // Marker interface
    }

    /**
     * Returns the YANG {@code identity} of this datastore.
     *
     * @return the YANG identity of this datastore
     */
    Name name();

    ReadAccess readAccess();

    WriteAccess writeAccess();

    ReadWriteAccess readWriteAccess();

    /**
     * A name of {@link DOMDatastore} derived from the {@link Datastore} identity hierarchy.
     *
     * @param qname the {@link QName} of a concrete datastore identity.
     */
    record Name(QName qname) {
        public Name {
            if (qname.equals(Datastore.QNAME) || qname.equals(Conventional.QNAME) || qname.equals(Dynamic.QNAME)) {
                throw new IllegalArgumentException("Cannot instantiate abstract base " + qname);
            }
        }
    }

    sealed interface Access permits ReadAccess, WriteAccess {
        // Just an API marker
    }

    non-sealed interface ReadAccess extends Access {
        // FIXME: plain put() etc. operations

        ReadTransaction newReadTransaction();
    }

    interface ReadWriteAccess extends ReadAccess, WriteAccess {

        ReadWriteTransaction newReadWriteTransaction();
    }

    non-sealed interface WriteAccess extends Access {

        WriteTransaction newWriteTransaction();
    }
}
