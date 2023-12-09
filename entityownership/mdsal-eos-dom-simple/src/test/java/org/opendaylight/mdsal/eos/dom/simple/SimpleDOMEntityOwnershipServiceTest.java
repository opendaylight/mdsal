/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.dom.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipChange;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListener;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;

@ExtendWith(MockitoExtension.class)
class SimpleDOMEntityOwnershipServiceTest {
    private static final String FOO_TYPE = "foo";
    private static final String BAR_TYPE = "bar";
    private static final DOMEntity FOO_FOO_ENTITY = new DOMEntity(FOO_TYPE, "foo");
    private static final DOMEntity FOO_BAR_ENTITY = new DOMEntity(FOO_TYPE, "bar");

    final DOMEntityOwnershipService service = new SimpleDOMEntityOwnershipService();

    @Test
    void testNonExistingEntity() {
        assertFalse(service.isCandidateRegistered(FOO_FOO_ENTITY));
        assertEquals(Optional.empty(), service.getOwnershipState(FOO_FOO_ENTITY));
    }

    @Test
    void testExistingEntity() throws Exception {
        try (var reg = service.registerCandidate(FOO_FOO_ENTITY)) {
            assertNotNull(reg);

            assertTrue(service.isCandidateRegistered(FOO_FOO_ENTITY));
            assertFalse(service.isCandidateRegistered(FOO_BAR_ENTITY));

            assertEquals(Optional.of(EntityOwnershipState.IS_OWNER), service.getOwnershipState(FOO_FOO_ENTITY));
        }
        assertFalse(service.isCandidateRegistered(FOO_FOO_ENTITY));
    }

    @Test
    void testDuplicateRegistration() throws Exception {
        assertNotNull(service.registerCandidate(FOO_FOO_ENTITY));

        // Should throw
        assertThrows(CandidateAlreadyRegisteredException.class, () -> service.registerCandidate(FOO_FOO_ENTITY));
    }

    @Test
    void testListener() throws Exception {
        final var entityReg = service.registerCandidate(FOO_FOO_ENTITY);
        assertNotNull(entityReg);

        // Mismatched type, not triggered
        final var barListener = mock(DOMEntityOwnershipListener.class);
        try (var barReg = service.registerListener(BAR_TYPE, barListener)) {
            // Matching type should be triggered
            final var fooListener = mock(DOMEntityOwnershipListener.class);
            doNothing().when(fooListener).ownershipChanged(any(EntityOwnershipChange.class));
            try (var fooReg = service.registerListener(FOO_TYPE, fooListener)) {
                final var fooCaptor = ArgumentCaptor.forClass(EntityOwnershipChange.class);
                verify(fooListener).ownershipChanged(fooCaptor.capture());

                var fooChange = fooCaptor.getValue();
                assertEquals(FOO_FOO_ENTITY, fooChange.getEntity());
                assertEquals(EntityOwnershipStateChange.LOCAL_OWNERSHIP_GRANTED, fooChange.getState());

                reset(fooListener);
                doNothing().when(fooListener).ownershipChanged(any(EntityOwnershipChange.class));
                entityReg.close();
                verify(fooListener).ownershipChanged(fooCaptor.capture());
                fooChange = fooCaptor.getValue();
                assertEquals(FOO_FOO_ENTITY, fooChange.getEntity());
                assertEquals(EntityOwnershipStateChange.LOCAL_OWNERSHIP_LOST_NO_OWNER, fooChange.getState());
            }
        }
    }

    @Test
    void testToString() throws Exception {
        final var uuid = UUID.randomUUID();
        final var expected = String.format("SimpleDOMEntityOwnershipService{uuid=%s, entities={}, listeners={}}", uuid);
        assertEquals(expected, new SimpleDOMEntityOwnershipService(uuid).toString());
    }
}
