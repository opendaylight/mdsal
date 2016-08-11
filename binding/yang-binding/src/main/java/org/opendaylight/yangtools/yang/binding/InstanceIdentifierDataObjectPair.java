/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.util.Objects;
import org.opendaylight.yangtools.util.EvenMoreObjects;

/**
 * Pair of an {@link InstanceIdentifier} &amp; {@link DataObject}.
 *
 * @author Michael Vorburger
 */
public class InstanceIdentifierDataObjectPair<T extends DataObject> {

    private final InstanceIdentifier<T> instanceIdentifier;
    private final T dataObject;

    public static <T extends DataObject> InstanceIdentifierDataObjectPair<T> of(
            InstanceIdentifier<T> instanceIdentifier, T dataObject) {
        return new InstanceIdentifierDataObjectPair<>(instanceIdentifier, dataObject);
    }

    private InstanceIdentifierDataObjectPair(InstanceIdentifier<T> instanceIdentifier, T dataObject) {
        this.instanceIdentifier = instanceIdentifier;
        this.dataObject = dataObject;
    }

    public InstanceIdentifier<T> getInstanceIdentifier() {
        return instanceIdentifier;
    }

    public T getDataObject() {
        return dataObject;
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceIdentifier, dataObject);
    }

    @Override
    public boolean equals(Object obj) {
        return EvenMoreObjects.equalsHelper(this, obj,
            (one, another) -> Objects.equals(one.instanceIdentifier, another.instanceIdentifier)
                    && Objects.equals(one.dataObject, another.dataObject));
    }

    @Override
    public String toString() {
        return new StringBuilder("InstanceIdentifierDataObjectPair: ").append(instanceIdentifier).append(" => ")
                .append(dataObject).toString();
    }
}
