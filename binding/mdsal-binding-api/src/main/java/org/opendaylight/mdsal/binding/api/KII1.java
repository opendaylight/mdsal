/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Key;
import org.opendaylight.yangtools.yang.binding.KeyAware;

final class KII1<T extends KeyAware<K> & DataObject, K extends Key<T>> extends II1<T> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private @Nullable K key;

    @SuppressWarnings("redundantModifier")
    public KII1() {
        // For Externalizable
    }

    KII1(final KeyedInstanceIdentifier<T, K> source) {
        super(source);
        key = source.getKey();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(key);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        key = (K) in.readObject();
    }

    @Override
    @java.io.Serial
    Object readResolve() throws ObjectStreamException {
        return new KeyedInstanceIdentifier<>(getTargetType(), getPathArguments(), getHash(), key);
    }
}
