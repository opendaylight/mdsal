/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.clustering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.mdsal.binding.api.clustering.Entity;
import org.opendaylight.mdsal.binding.api.clustering.EntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.binding.api.clustering.EntityOwnershipChange;
import org.opendaylight.mdsal.binding.api.clustering.EntityOwnershipListener;
import org.opendaylight.mdsal.binding.api.clustering.EntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.binding.dom.adapter.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.common.api.clustering.EntityOwnershipChangeState;
import org.opendaylight.mdsal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.mdsal.dom.api.clustering.DOMEntity;
import org.opendaylight.mdsal.dom.api.clustering.DOMEntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.dom.api.clustering.DOMEntityOwnershipChange;
import org.opendaylight.mdsal.dom.api.clustering.DOMEntityOwnershipListener;
import org.opendaylight.mdsal.dom.api.clustering.DOMEntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.dom.api.clustering.DOMEntityOwnershipService;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.impl.GeneratedClassLoadingStrategy;

/**
 * Unit tests for BindingDOMEntityOwnershipServiceAdapter.
 *
 * @author Thomas Pantelis
 */
public class BindingDOMEntityOwnershipServiceAdapterTest {
    static Entity BINDING_ENTITY = new Entity("foo", "bar");
    static DOMEntity DOM_ENTITY = new DOMEntity("foo", "bar");

    @Mock
    private BindingNormalizedNodeCodecRegistry mockCodecRegistry;

    @Mock
    private DOMEntityOwnershipService mockDOMService;

    private BindingDOMEntityOwnershipServiceAdapter adapter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        doReturn(DOM_ENTITY.getIdentifier()).when(mockCodecRegistry).toYangInstanceIdentifier(BINDING_ENTITY.getIdentifier());
        doReturn(BINDING_ENTITY.getIdentifier()).when(mockCodecRegistry).fromYangInstanceIdentifier(DOM_ENTITY.getIdentifier());

        adapter = new BindingDOMEntityOwnershipServiceAdapter(mockDOMService,
                new BindingToNormalizedNodeCodec(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(),
                        mockCodecRegistry));
    }

    @Test
    public void testRegisterCandidate() throws CandidateAlreadyRegisteredException {
        DOMEntityOwnershipCandidateRegistration mockDOMReg = mock(DOMEntityOwnershipCandidateRegistration.class);
        doReturn(mockDOMReg).when(mockDOMService).registerCandidate(DOM_ENTITY);

        EntityOwnershipCandidateRegistration reg = adapter.registerCandidate(BINDING_ENTITY);

        assertNotNull("registerCandidate returned null", reg);
        assertEquals("getInstance", BINDING_ENTITY, reg.getInstance());

        reg.close();
        verify(mockDOMReg).close();
    }

    @Test
    public void testRegisterListener() {
        DOMEntityOwnershipListenerRegistration mockDOMReg = mock(DOMEntityOwnershipListenerRegistration.class);
        doReturn(mockDOMReg).when(mockDOMService).registerListener(eq(DOM_ENTITY.getType()),
                any(DOMEntityOwnershipListener.class));
        EntityOwnershipListener mockListener = mock(EntityOwnershipListener.class);

        EntityOwnershipListenerRegistration reg = adapter.registerListener(BINDING_ENTITY.getType(), mockListener);

        assertNotNull("registerListener returned null", reg);
        assertEquals("getInstance", mockListener, reg.getInstance());
        assertEquals("getEntityType", BINDING_ENTITY.getType(), reg.getEntityType());

        ArgumentCaptor<DOMEntityOwnershipListener> domListenerCaptor = ArgumentCaptor.forClass(DOMEntityOwnershipListener.class);
        verify(mockDOMService).registerListener(eq(DOM_ENTITY.getType()),  domListenerCaptor.capture());

        DOMEntityOwnershipChange domOwnershipChange = new DOMEntityOwnershipChange(DOM_ENTITY,
                EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED);
        domListenerCaptor.getValue().ownershipChanged(domOwnershipChange );

        ArgumentCaptor<EntityOwnershipChange> ownershipChangeCaptor = ArgumentCaptor.forClass(EntityOwnershipChange.class);
        verify(mockListener).ownershipChanged(ownershipChangeCaptor.capture());

        EntityOwnershipChange change = ownershipChangeCaptor.getValue();
        assertEquals("getEntity", BINDING_ENTITY, change.getEntity());
        assertEquals("getState", EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED, change.getState());

        reg.close();
        verify(mockDOMReg).close();
    }

    @Test
    public void testGetOwnershipState() {
        Optional<EntityOwnershipState>  expectedState = Optional.of(EntityOwnershipState.IS_OWNER);
        doReturn(expectedState).when(mockDOMService).getOwnershipState(DOM_ENTITY);

        Optional<EntityOwnershipState> actualState = adapter.getOwnershipState(BINDING_ENTITY);

        assertSame("getOwnershipState", expectedState, actualState);
    }
}
