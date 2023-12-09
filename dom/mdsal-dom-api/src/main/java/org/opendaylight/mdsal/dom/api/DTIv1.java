/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * A serialization proxy for {@link DOMDataTreeIdentifier}.
 */
final class DTIv1 implements Externalizable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private DOMDataTreeIdentifier id;

    @SuppressWarnings("redundantModifier")
    public DTIv1() {
        // For Externalizable
    }

    DTIv1(final DOMDataTreeIdentifier id) {
        this.id = requireNonNull(id);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        id.getDatastoreType().writeTo(out);
        out.writeObject(id.getRootIdentifier());
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        id = new DOMDataTreeIdentifier(LogicalDatastoreType.readFrom(in), (YangInstanceIdentifier) in.readObject());
    }

    @java.io.Serial
    Object readResolve() {
        return verifyNotNull(id);
    }
}
