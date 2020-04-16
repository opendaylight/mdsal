/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.dom.adapter.ConstantAdapterContext;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecServices;
import org.opendaylight.mdsal.eos.binding.api.Entity;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipChange;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListener;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipChangeState;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipChange;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListener;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;

/**
 * Unit tests for BindingDOMEntityOwnershipServiceAdapter.
 *
 * @author Thomas Pantelis
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class BindingDOMEntityOwnershipServiceAdapterTest {
    static Entity BINDING_ENTITY = new Entity("foo", "bar");
    static DOMEntity DOM_ENTITY = new DOMEntity("foo", "bar");

    @Mock
    private BindingDOMCodecServices mockCodecRegistry;

    @Mock
    private DOMEntityOwnershipService mockDOMService;

    private BindingDOMEntityOwnershipServiceAdapter adapter;

    @Before
    public void setup() {
        doReturn(DOM_ENTITY.getIdentifier()).when(mockCodecRegistry).toYangInstanceIdentifier(
                BINDING_ENTITY.getIdentifier());
        doReturn(BINDING_ENTITY.getIdentifier()).when(mockCodecRegistry).fromYangInstanceIdentifier(
                DOM_ENTITY.getIdentifier());

        this.adapter = new BindingDOMEntityOwnershipServiceAdapter(mockDOMService,
            new ConstantAdapterContext(mockCodecRegistry));
    }

    @Test
    public void testRegisterCandidate() throws CandidateAlreadyRegisteredException {
        final DOMEntityOwnershipCandidateRegistration mockDOMReg = mock(DOMEntityOwnershipCandidateRegistration.class);
        doReturn(mockDOMReg).when(this.mockDOMService).registerCandidate(DOM_ENTITY);

        final EntityOwnershipCandidateRegistration reg = this.adapter.registerCandidate(BINDING_ENTITY);

        assertNotNull("registerCandidate returned null", reg);
        assertEquals("getInstance", BINDING_ENTITY, reg.getInstance());

        reg.close();
        verify(mockDOMReg).close();
    }

    @Test
    public void testRegisterListener() {
        final DOMEntityOwnershipListenerRegistration mockDOMReg = mock(DOMEntityOwnershipListenerRegistration.class);
        doReturn(mockDOMReg).when(this.mockDOMService).registerListener(eq(DOM_ENTITY.getType()),
                any(DOMEntityOwnershipListener.class));
        final EntityOwnershipListener mockListener = mock(EntityOwnershipListener.class);

        final EntityOwnershipListenerRegistration reg = this.adapter.registerListener(
                BINDING_ENTITY.getType(), mockListener);

        assertNotNull("registerListener returned null", reg);
        assertEquals("getInstance", mockListener, reg.getInstance());
        assertEquals("getEntityType", BINDING_ENTITY.getType(), reg.getEntityType());

        final ArgumentCaptor<DOMEntityOwnershipListener> domListenerCaptor = ArgumentCaptor.forClass(
                DOMEntityOwnershipListener.class);
        verify(this.mockDOMService).registerListener(eq(DOM_ENTITY.getType()),  domListenerCaptor.capture());

        final DOMEntityOwnershipChange domOwnershipChange = new DOMEntityOwnershipChange(DOM_ENTITY,
                EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED, true);
        domListenerCaptor.getValue().ownershipChanged(domOwnershipChange);

        final ArgumentCaptor<EntityOwnershipChange> ownershipChangeCaptor = ArgumentCaptor.forClass(
                EntityOwnershipChange.class);
        verify(mockListener).ownershipChanged(ownershipChangeCaptor.capture());

        final EntityOwnershipChange change = ownershipChangeCaptor.getValue();
        assertEquals("getEntity", BINDING_ENTITY, change.getEntity());
        assertEquals("getState", EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED, change.getState());

        reg.close();
        verify(mockDOMReg).close();
    }

    @Test
    public void testGetOwnershipState() {
        final Optional<EntityOwnershipState>  expectedState = Optional.of(EntityOwnershipState.IS_OWNER);
        doReturn(expectedState).when(this.mockDOMService).getOwnershipState(DOM_ENTITY);

        final Optional<EntityOwnershipState> actualState = this.adapter.getOwnershipState(BINDING_ENTITY);

        assertSame("getOwnershipState", expectedState, actualState);
    }

    @Test
    public void testIsCandidateRegistered() {
        doReturn(true).when(this.mockDOMService).isCandidateRegistered(DOM_ENTITY);
        assertEquals("isCandidateRegistered", true, this.adapter.isCandidateRegistered(BINDING_ENTITY));
    }

    public void testOwnershipChangeWithException() {
        final DOMEntityOwnershipListenerAdapter domEntityOwnershipListenerAdapter =
                new DOMEntityOwnershipListenerAdapter(mock(EntityOwnershipListener.class),
                    new ConstantAdapterContext(mockCodecRegistry));
        final DOMEntityOwnershipChange domOwnershipChange = mock(DOMEntityOwnershipChange.class);
        doThrow(IllegalStateException.class).when(domOwnershipChange).getEntity();
        assertThrows(IllegalStateException.class,
            () -> domEntityOwnershipListenerAdapter.ownershipChanged(domOwnershipChange));
    }
}
