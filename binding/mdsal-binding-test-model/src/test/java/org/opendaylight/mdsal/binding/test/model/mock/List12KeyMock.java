/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model.mock;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.processing.Generated;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.of.migration.test.model.rev150210.aug.grouping.list1.List12;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.binding.Identifier;

/**
 * This class represents the key of {@link List12} class.
 *
 * @see List12
 *
 */
@Generated("mdsal-binding-generator")
public class List12KeyMock
        implements Identifier<List12Mock> {
    private static final long serialVersionUID = 4712004729913052166L;
    private final Integer attrInt;


    /**
     * Constructs an instance.
     *
     * @param attrInt the entity attrInt
     * @throws NullPointerException if any of the arguments are null
     */
    public List12KeyMock(@NonNull Integer attrInt) {
        this.attrInt = CodeHelpers.requireKeyProp(attrInt, "attrInt");
    }

    /**
     * Creates a copy from Source Object.
     *
     * @param source Source object
     */
    public List12KeyMock(List12KeyMock source) {
        this.attrInt = source.attrInt;
    }


    /**
     * Return attrInt, guaranteed to be non-null.
     *
     * @return {@code Integer} attrInt, guaranteed to be non-null.
     */
    public @NonNull Integer getAttrInt() {
        return attrInt;
    }


    @Override
    public int hashCode() {
        return CodeHelpers.wrapperHashCode(attrInt);
    }

    @Override
    public final boolean equals(Object obj) {
        return this == obj || obj instanceof List12KeyMock other
                && Objects.equals(attrInt, other.attrInt);
    }

    @Override
    public String toString() {
        final var helper = MoreObjects.toStringHelper(
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.of
                        .migration.test.model.rev150210.aug.grouping.list1.List12Key.class);
        CodeHelpers.appendValue(helper, "attrInt", attrInt);
        return helper.toString();
    }
}


