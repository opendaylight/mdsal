/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.DataObject;

class II1<T extends DataObject> implements Externalizable {
    @java.io.Serial
    private static final long serialVersionUID = 3L;

    private @Nullable Iterable<PathArgument> pathArguments;
    private @Nullable Class<T> targetType;
    private int hash;

    @SuppressWarnings("redundantModifier")
    public II1() {
        // For Externalizable
    }

    II1(final InstanceIdentifier<T> source) {
        pathArguments = source.pathArguments;
        targetType = source.getTargetType();
        hash = source.hashCode();
    }

    final int getHash() {
        return hash;
    }

    final Iterable<PathArgument> getPathArguments() {
        return pathArguments;
    }

    final Class<T> getTargetType() {
        return targetType;
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeObject(targetType);
        out.writeInt(hash);
        out.writeInt(Iterables.size(pathArguments));
        for (Object o : pathArguments) {
            out.writeObject(o);
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        targetType = (Class<T>) in.readObject();
        hash = in.readInt();

        final int size = in.readInt();
        final List<PathArgument> args = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            args.add((PathArgument) in.readObject());
        }
        pathArguments = ImmutableList.copyOf(args);
    }

    @java.io.Serial
    Object readResolve() throws ObjectStreamException {
        return new InstanceIdentifier<>(targetType, pathArguments, hash);
    }
}
