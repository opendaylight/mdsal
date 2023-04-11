/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.dom.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipChangeState;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipChange;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListener;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SimpleDOMEntityOwnershipServiceTest {
    public static final String FOO_TYPE = "foo";
    public static final String BAR_TYPE = "bar";

    public static final DOMEntity FOO_FOO_ENTITY = new DOMEntity(FOO_TYPE, "foo");
    public static final DOMEntity FOO_BAR_ENTITY = new DOMEntity(FOO_TYPE, "bar");

    public final DOMEntityOwnershipService service = new SimpleDOMEntityOwnershipService();

    @Test
    public void testNonExistingEntity() {
        assertFalse(service.isCandidateRegistered(FOO_FOO_ENTITY));
        final Optional<EntityOwnershipState> state = service.getOwnershipState(FOO_FOO_ENTITY);
        assertNotNull(state);
        assertFalse(state.isPresent());
    }

    @Test
    public void testExistingEntity() throws CandidateAlreadyRegisteredException {
        final DOMEntityOwnershipCandidateRegistration reg = service.registerCandidate(FOO_FOO_ENTITY);
        assertNotNull(reg);

        assertTrue(service.isCandidateRegistered(FOO_FOO_ENTITY));
        assertFalse(service.isCandidateRegistered(FOO_BAR_ENTITY));

        final Optional<EntityOwnershipState> state = service.getOwnershipState(FOO_FOO_ENTITY);
        assertNotNull(state);
        assertTrue(state.isPresent());
        assertEquals(EntityOwnershipState.IS_OWNER, state.orElseThrow());

        reg.close();
        assertFalse(service.isCandidateRegistered(FOO_FOO_ENTITY));
    }

    @Test(expected = CandidateAlreadyRegisteredException.class)
    public void testDuplicateRegistration() throws CandidateAlreadyRegisteredException {
        final DOMEntityOwnershipCandidateRegistration reg = service.registerCandidate(FOO_FOO_ENTITY);
        assertNotNull(reg);

        // Should throw
        service.registerCandidate(FOO_FOO_ENTITY);
    }

    @Test
    public void testListener() throws CandidateAlreadyRegisteredException {
        final DOMEntityOwnershipCandidateRegistration entityReg = service.registerCandidate(FOO_FOO_ENTITY);
        assertNotNull(entityReg);

        // Mismatched type, not triggered
        final DOMEntityOwnershipListener barListener = mock(DOMEntityOwnershipListener.class);
        final DOMEntityOwnershipListenerRegistration barReg = service.registerListener(BAR_TYPE, barListener);

        // Matching type should be triggered
        final DOMEntityOwnershipListener fooListener = mock(DOMEntityOwnershipListener.class);
        doNothing().when(fooListener).ownershipChanged(any(DOMEntityOwnershipChange.class));
        final DOMEntityOwnershipListenerRegistration fooReg = service.registerListener(FOO_TYPE, fooListener);
        final ArgumentCaptor<DOMEntityOwnershipChange> fooCaptor = ArgumentCaptor.forClass(
            DOMEntityOwnershipChange.class);
        verify(fooListener).ownershipChanged(fooCaptor.capture());

        DOMEntityOwnershipChange fooChange = fooCaptor.getValue();
        assertEquals(FOO_FOO_ENTITY, fooChange.getEntity());
        assertEquals(EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED, fooChange.getState());

        reset(fooListener);
        doNothing().when(fooListener).ownershipChanged(any(DOMEntityOwnershipChange.class));
        entityReg.close();
        verify(fooListener).ownershipChanged(fooCaptor.capture());
        fooChange = fooCaptor.getValue();
        assertEquals(FOO_FOO_ENTITY, fooChange.getEntity());
        assertEquals(EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NO_OWNER, fooChange.getState());

        fooReg.close();
        barReg.close();
    }

    @Test
    public void testToString() throws CandidateAlreadyRegisteredException {
        final UUID uuid = UUID.randomUUID();
        final String expected = String.format("SimpleDOMEntityOwnershipService{uuid=%s, entities={}, listeners={}}",
            uuid);
        assertEquals(expected, new SimpleDOMEntityOwnershipService(uuid).toString());
    }
}
