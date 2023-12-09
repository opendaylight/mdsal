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
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListener;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipChange;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListener;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * Unit tests for BindingDOMEntityOwnershipServiceAdapter.
 *
 * @author Thomas Pantelis
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DefaultEntityOwnershipServiceTest {
    static final Entity BINDING_ENTITY = new Entity("foo", "bar");
    static final DOMEntity DOM_ENTITY = new DOMEntity("foo", "bar");

    @Mock
    private BindingDOMCodecServices mockCodecRegistry;

    @Mock
    private DOMEntityOwnershipService mockDOMService;

    private DefaultEntityOwnershipService adapter;

    @Before
    public void setup() {
        doReturn(DOM_ENTITY.getIdentifier()).when(mockCodecRegistry).toYangInstanceIdentifier(
                BINDING_ENTITY.getIdentifier());
        doReturn(BINDING_ENTITY.getIdentifier()).when(mockCodecRegistry).fromYangInstanceIdentifier(
                DOM_ENTITY.getIdentifier());
        adapter = new DefaultEntityOwnershipService(mockDOMService, new ConstantAdapterContext(mockCodecRegistry));
    }

    @Test
    public void testRegisterCandidate() throws CandidateAlreadyRegisteredException {
        final var mockDOMReg = mock(Registration.class);
        doReturn(mockDOMReg).when(mockDOMService).registerCandidate(DOM_ENTITY);

        try (var reg = adapter.registerCandidate(BINDING_ENTITY)) {
            assertNotNull("registerCandidate returned null", reg);
        }
        verify(mockDOMReg).close();
    }

    @Test
    public void testRegisterListener() {
        final var mockDOMReg = mock(Registration.class);
        doReturn(mockDOMReg).when(mockDOMService).registerListener(eq(DOM_ENTITY.getType()),
                any(DOMEntityOwnershipListener.class));
        final var mockListener = mock(EntityOwnershipListener.class);

        try (var reg = adapter.registerListener(BINDING_ENTITY.getType(), mockListener)) {
            assertNotNull("registerListener returned null", reg);

            final var domListenerCaptor = ArgumentCaptor.forClass(DOMEntityOwnershipListener.class);
            verify(mockDOMService).registerListener(eq(DOM_ENTITY.getType()),  domListenerCaptor.capture());

            final var domOwnershipChange = new EntityOwnershipChange<>(DOM_ENTITY,
                EntityOwnershipStateChange.LOCAL_OWNERSHIP_GRANTED, true);
            domListenerCaptor.getValue().ownershipChanged(domOwnershipChange);

            final var ownershipChangeCaptor = ArgumentCaptor.forClass(EntityOwnershipChange.class);
            verify(mockListener).ownershipChanged(ownershipChangeCaptor.capture());

            final var change = ownershipChangeCaptor.getValue();
            assertEquals("getEntity", BINDING_ENTITY, change.getEntity());
            assertEquals("getState", EntityOwnershipStateChange.LOCAL_OWNERSHIP_GRANTED, change.getState());
        }
        verify(mockDOMReg).close();
    }

    @Test
    public void testGetOwnershipState() {
        final var expectedState = Optional.of(EntityOwnershipState.IS_OWNER);
        doReturn(expectedState).when(mockDOMService).getOwnershipState(DOM_ENTITY);

        final var actualState = adapter.getOwnershipState(BINDING_ENTITY);
        assertSame("getOwnershipState", expectedState, actualState);
    }

    @Test
    public void testIsCandidateRegistered() {
        doReturn(true).when(mockDOMService).isCandidateRegistered(DOM_ENTITY);
        assertEquals("isCandidateRegistered", true, adapter.isCandidateRegistered(BINDING_ENTITY));
    }

    @Test
    public void testOwnershipChangeWithException() {
        final var listener = mock(EntityOwnershipListener.class);
        doThrow(IllegalStateException.class).when(listener).ownershipChanged(any());

        final var domEntityOwnershipListenerAdapter = new DOMEntityOwnershipListenerAdapter(listener,
                new ConstantAdapterContext(mockCodecRegistry));
        final var domOwnershipChange = new EntityOwnershipChange<>(DOM_ENTITY,
            EntityOwnershipStateChange.LOCAL_OWNERSHIP_GRANTED);

        domEntityOwnershipListenerAdapter.ownershipChanged(domOwnershipChange);
    }
}
