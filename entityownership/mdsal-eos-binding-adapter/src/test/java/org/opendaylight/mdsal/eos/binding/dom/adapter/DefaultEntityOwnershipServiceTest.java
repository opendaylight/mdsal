/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.binding.dom.adapter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.binding.dom.adapter.ConstantAdapterContext;
import org.opendaylight.mdsal.eos.binding.api.Entity;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListener;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListener;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecServices;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * Unit tests for BindingDOMEntityOwnershipServiceAdapter.
 *
 * @author Thomas Pantelis
 */
@ExtendWith(MockitoExtension.class)
class DefaultEntityOwnershipServiceTest {
    private static final @NonNull Entity BINDING_ENTITY = new Entity("foo", "bar");
    private static final @NonNull DOMEntity DOM_ENTITY = new DOMEntity("foo", "bar");

    @Mock
    private BindingDOMCodecServices mockCodecRegistry;
    @Mock
    private DOMEntityOwnershipService mockDOMService;

    private DefaultEntityOwnershipService adapter;

    @BeforeEach
    void setup() {
        adapter = new DefaultEntityOwnershipService(mockDOMService, new ConstantAdapterContext(mockCodecRegistry));
    }

    private void mockToBinding() {
        doReturn(BINDING_ENTITY.getIdentifier()).when(mockCodecRegistry).fromYangInstanceIdentifier(
            DOM_ENTITY.getIdentifier());
    }

    private void mockToDom() {
        doReturn(DOM_ENTITY.getIdentifier()).when(mockCodecRegistry)
            .toYangInstanceIdentifier(BINDING_ENTITY.getIdentifier());
    }

    @Test
    void testRegisterCandidate() throws Exception {
        mockToDom();
        final var mockDOMReg = mock(Registration.class);
        doReturn(mockDOMReg).when(mockDOMService).registerCandidate(DOM_ENTITY);

        try (var reg = adapter.registerCandidate(BINDING_ENTITY)) {
            assertNotNull(reg);
        }
    }

    @Test
    void testRegisterListener() {
        mockToBinding();

        final var mockDOMReg = mock(Registration.class);
        final var domListenerCaptor = ArgumentCaptor.forClass(DOMEntityOwnershipListener.class);
        doReturn(mockDOMReg).when(mockDOMService).registerListener(eq(DOM_ENTITY.getType()),
            domListenerCaptor.capture());
        final var mockListener = mock(EntityOwnershipListener.class);

        try (var reg = adapter.registerListener(BINDING_ENTITY.getType(), mockListener)) {
            assertNotNull(reg);

            doNothing().when(mockListener).ownershipChanged(BINDING_ENTITY,
                EntityOwnershipStateChange.LOCAL_OWNERSHIP_GRANTED, true);
            domListenerCaptor.getValue().ownershipChanged(DOM_ENTITY,
                EntityOwnershipStateChange.LOCAL_OWNERSHIP_GRANTED, true);
        }
    }

    @Test
    void testGetOwnershipState() {
        mockToDom();

        final var expectedState = Optional.of(EntityOwnershipState.IS_OWNER);
        doReturn(expectedState).when(mockDOMService).getOwnershipState(DOM_ENTITY);
        assertSame(expectedState, adapter.getOwnershipState(BINDING_ENTITY));
    }

    @Test
    void testIsCandidateRegistered() {
        mockToDom();

        doReturn(true).when(mockDOMService).isCandidateRegistered(DOM_ENTITY);
        assertTrue(adapter.isCandidateRegistered(BINDING_ENTITY));
    }

    @Test
    void testOwnershipChangeWithException() {
        mockToBinding();

        final var listener = mock(EntityOwnershipListener.class);
        doThrow(IllegalStateException.class).when(listener).ownershipChanged(BINDING_ENTITY,
            EntityOwnershipStateChange.LOCAL_OWNERSHIP_GRANTED, false);

        final var domAdapter = new DOMEntityOwnershipListenerAdapter(listener,
            new ConstantAdapterContext(mockCodecRegistry));

        domAdapter.ownershipChanged(DOM_ENTITY, EntityOwnershipStateChange.LOCAL_OWNERSHIP_GRANTED, false);
    }
}
