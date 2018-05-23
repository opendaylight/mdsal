/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util;

import static org.opendaylight.mdsal.binding.model.util.Types.parameterizedTypeFor;
import static org.opendaylight.mdsal.binding.model.util.Types.typeForClass;

import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.binding.annotations.RoutingContext;

public final class BindingTypes {

    public static final ConcreteType AUGMENTABLE = typeForClass(Augmentable.class);
    public static final ConcreteType AUGMENTATION = typeForClass(Augmentation.class);
    public static final ConcreteType BASE_IDENTITY = typeForClass(BaseIdentity.class);
    public static final ConcreteType DATA_CONTAINER = typeForClass(DataContainer.class);
    public static final ConcreteType DATA_OBJECT = typeForClass(DataObject.class);
    public static final ConcreteType DATA_ROOT = typeForClass(DataRoot.class);
    public static final ConcreteType IDENTIFIABLE = typeForClass(Identifiable.class);
    public static final ConcreteType IDENTIFIER = typeForClass(Identifier.class);
    public static final ConcreteType INSTANCE_IDENTIFIER = typeForClass(InstanceIdentifier.class);
    public static final ConcreteType NOTIFICATION = typeForClass(Notification.class);
    public static final ConcreteType NOTIFICATION_LISTENER = typeForClass(NotificationListener.class);
    public static final ConcreteType RPC_SERVICE = typeForClass(RpcService.class);

    // This is an annotation, we are current just referencing the type
    public static final JavaTypeName ROUTING_CONTEXT = JavaTypeName.create(RoutingContext.class);

    private static final ConcreteType CHILD_OF = typeForClass(ChildOf.class);
    private static final ConcreteType CHOICE_IN = typeForClass(ChoiceIn.class);

    private BindingTypes() {

    }

    /**
     * Specialize {@link Augmentable} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code Augmentable<Type>}
     * @throws NullPointerException if {@code type} is null
     */
    public static ParameterizedType augmentable(final Type type) {
        return parameterizedTypeFor(AUGMENTABLE, type);
    }

    /**
     * Specialize {@link ChildOf} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code ChildOf<Type>}
     * @throws NullPointerException if {@code type} is null
     */
    public static ParameterizedType childOf(final Type type) {
        return parameterizedTypeFor(CHILD_OF, type);
    }

    /**
     * Type specializing {@link ChoiceIn} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code ChoiceIn<Type>}
     * @throws NullPointerException if {@code type} is null
     */
    public static ParameterizedType choiceIn(final Type type) {
        return parameterizedTypeFor(CHOICE_IN, type);
    }
}
