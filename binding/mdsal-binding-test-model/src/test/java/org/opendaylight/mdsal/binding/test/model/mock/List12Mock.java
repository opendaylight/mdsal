/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model.mock;

import com.google.common.base.MoreObjects;
import java.lang.Class;
import java.lang.Integer;
import java.lang.NullPointerException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.NoSuchElementException;
import java.util.Objects;
import javax.annotation.processing.Generated;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.of.migration.test.model.rev150210.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.of.migration.test.model.rev150210.aug.grouping.list1.List12Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.of.migration.test.model.rev150210.aug.grouping.list1.List12Key;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 *
 * <p>
 * This class represents the following YANG schema fragment defined in module <b>opendaylight-of-migration-test-model</b>
 * <pre>
 * list list1-2 {
 *   key attr-int;
 *   leaf attr-int {
 *     type int32;
 *   }
 *   leaf attr-str {
 *     type string;
 *   }
 * }
 * </pre>
 * <p>To create instances of this class use {@link List12Builder}.
 * @see List12Builder
 * @see List12Key
 *
 */
@Generated("mdsal-binding-generator")
public interface List12Mock
        extends
        ChildOf<List1Mock>,
        Augmentable<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller
                .md.sal.of.migration.test.model.rev150210.aug.grouping.list1.List12>,
        Identifiable<List12KeyMock>
{



    /**
     * YANG identifier of the statement represented by this class.
     */
    public static final @NonNull QName QNAME = $YangModuleInfoImpl.qnameOf("list1-2");

    @Override
    default Class<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller
            .md.sal.of.migration.test.model.rev150210.aug.grouping.list1.List12> implementedInterface() {
        return org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller
                .md.sal.of.migration.test.model.rev150210.aug.grouping.list1.List12.class;
    }

    /**
     * Default implementation of {@link Object#hashCode()} contract for this interface.
     * Implementations of this interface are encouraged to defer to this method to get consistent hashing
     * results across all implementations.
     *
     * @param obj Object for which to generate hashCode() result.
     * @return Hash code value of data modeled by this interface.
     * @throws NullPointerException if {@code obj} is null
     */
    static int bindingHashCode(final org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller
            .md.sal.of.migration.test.model.rev150210.aug.grouping.list1.@NonNull List12 obj) {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(obj.getAttrInt());
        result = prime * result + Objects.hashCode(obj.getAttrStr());
        for (var augmentation : obj.augmentations().values()) {
            result += augmentation.hashCode();
        }
        return result;
    }

    /**
     * Default implementation of {@link Object#equals(Object)} contract for this interface.
     * Implementations of this interface are encouraged to defer to this method to get consistent equality
     * results across all implementations.
     *
     * @param thisObj Object acting as the receiver of equals invocation
     * @param obj Object acting as argument to equals invocation
     * @return True if thisObj and obj are considered equal
     * @throws NullPointerException if {@code thisObj} is null
     */
    static boolean bindingEquals(final org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller
            .md.sal.of.migration.test.model.rev150210.aug.grouping.list1.@NonNull List12 thisObj, final Object obj) {
        if (thisObj == obj) {
            return true;
        }
        final org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller
                .md.sal.of.migration.test.model.rev150210.aug.grouping.list1.List12 other
                = CodeHelpers.checkCast(org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller
                .md.sal.of.migration.test.model.rev150210.aug.grouping.list1.List12.class, obj);
        if (other == null) {
            return false;
        }
        if (!Objects.equals(thisObj.getAttrInt(), other.getAttrInt())) {
            return false;
        }
        if (!Objects.equals(thisObj.getAttrStr(), other.getAttrStr())) {
            return false;
        }
        return thisObj.augmentations().equals(other.augmentations());
    }

    /**
     * Default implementation of {@link Object#toString()} contract for this interface.
     * Implementations of this interface are encouraged to defer to this method to get consistent string
     * representations across all implementations.
     *
     * @param obj Object for which to generate toString() result.
     * @return {@link String} value of data modeled by this interface.
     * @throws NullPointerException if {@code obj} is null
     */
    static String bindingToString(final org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller
            .md.sal.of.migration.test.model.rev150210.aug.grouping.list1.@NonNull List12 obj) {
        final var helper = MoreObjects.toStringHelper("List12");
        CodeHelpers.appendValue(helper, "attrInt", obj.getAttrInt());
        CodeHelpers.appendValue(helper, "attrStr", obj.getAttrStr());
        CodeHelpers.appendAugmentations(helper, "augmentation", obj);
        return helper.toString();
    }

    @Override
    List12KeyMock key();

    /**
     * Return attrInt, or {@code null} if it is not present.
     *
     * @return {@code Integer} attrInt, or {@code null} if it is not present.
     *
     */
    Integer getAttrInt();

    /**
     * Return attrInt, guaranteed to be non-null.
     *
     * @return {@code Integer} attrInt, guaranteed to be non-null.
     * @throws NoSuchElementException if attrInt is not present
     *
     */
    default @NonNull Integer requireAttrInt() {
        return CodeHelpers.require(getAttrInt(), "attrint");
    }

    /**
     * Return attrStr, or {@code null} if it is not present.
     *
     * @return {@code String} attrStr, or {@code null} if it is not present.
     *
     */
    String getAttrStr();

    /**
     * Return attrStr, guaranteed to be non-null.
     *
     * @return {@code String} attrStr, guaranteed to be non-null.
     * @throws NoSuchElementException if attrStr is not present
     *
     */
    default @NonNull String requireAttrStr() {
        return CodeHelpers.require(getAttrStr(), "attrstr");
    }

}


