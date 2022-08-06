/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.ImmutableList;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;

@ExtendWith(MockitoExtension.class)
class KeyArchetypeTest {
    private static final JavaTypeName KEY_NAME = JavaTypeName.create("foo", "bar");
    private static final JavaTypeName KEY_AWARE_NAME = JavaTypeName.create("foo", "bar");
    private static final QName BAZ_QUX = QName.create("baz", "qux");

    @Mock
    private KeyEffectiveStatement stmt;
    @Mock
    private LeafEffectiveStatement leaf;

    @BeforeEach
    void beforeEach() {
        doReturn(Set.of(BAZ_QUX)).when(stmt).argument();
    }

    @Test
    void detectBadFieldsSize() {
        final var ex = assertThrows(IllegalArgumentException.class,
            () -> new KeyArchetype(KEY_NAME, stmt, KEY_AWARE_NAME, ImmutableList.of()));
        assertEquals("Expected fields for [(baz)qux], got []", ex.getMessage());
    }

    @Test
    void detectBadFieldName() {
        doReturn("xyzzy leaf").when(leaf).toString();
        doReturn(QName.create("xyzzy", "xyzzy")).when(leaf).argument();
        final var badField = new DataObjectField<>(leaf, "xyzzy", Type.of(Empty.class));

        final var ex = assertThrows(IllegalArgumentException.class,
            () -> new KeyArchetype(KEY_NAME, stmt, KEY_AWARE_NAME, ImmutableList.of(badField)));
        assertEquals("""
            Field DataObjectField[statement=xyzzy leaf, name=xyzzy, \
            type=DefaultType{identifier=org.opendaylight.yangtools.yang.common.Empty}, mechanics=NORMAL], \
            expecting all of [(baz)qux]""",
            ex.getMessage());
    }

    @Test
    void constructor() {
        doReturn(BAZ_QUX).when(leaf).argument();
        final var barQux = new DataObjectField<>(leaf, "barQux", Type.of(Empty.class));

        assertSame(JavaConstruct.Class.class,
            new KeyArchetype(KEY_NAME, stmt, KEY_AWARE_NAME, ImmutableList.of(barQux)).construct());
    }

}
