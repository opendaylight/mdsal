/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Objects;

/**
 * Pair of an {@link InstanceIdentifier} &amp; {@link DataObject}.
 *
 * @author Michael Vorburger
 */
public class InstanceIdentifierDataObjectPair<T extends DataObject>
        extends SimpleImmutableEntry<InstanceIdentifier<T>, T> {

    private static final long serialVersionUID = -2305057702847588373L;

    public static <T extends DataObject> InstanceIdentifierDataObjectPair<T> of(
            InstanceIdentifier<T> instanceIdentifier, T dataObject) {
        return new InstanceIdentifierDataObjectPair<>(instanceIdentifier, dataObject);
    }

    private InstanceIdentifierDataObjectPair(InstanceIdentifier<T> instanceIdentifier, T dataObject) {
        super(instanceIdentifier, dataObject);
    }

    public InstanceIdentifier<T> getInstanceIdentifier() {
        return getKey();
    }

    public T getDataObject() {
        return getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInstanceIdentifier(), getDataObject());
    }

    @Override
    public boolean equals(Object obj) {
        return EvenMoreObjects.equalsHelper(this, obj,
            (one, another) -> Objects.equals(one.instanceIdentifier, another.instanceIdentifier)
                    && Objects.equals(one.dataObject, another.dataObject));
    }

    @Override
    public String toString() {
        return new StringBuilder("InstanceIdentifierDataObjectPair: ").append(getInstanceIdentifier()).append(" => ")
                .append(getDataObject()).toString();
    }
}
