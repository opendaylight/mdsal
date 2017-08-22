/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.util;

import static org.opendaylight.mdsal.binding.javav2.generator.util.Types.parameterizedTypeFor;
import static org.opendaylight.mdsal.binding.javav2.generator.util.Types.typeForClass;

import org.opendaylight.mdsal.binding.javav2.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.javav2.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.spec.base.Action;
import org.opendaylight.mdsal.binding.javav2.spec.base.IdentifiableItem;
import org.opendaylight.mdsal.binding.javav2.spec.base.Identifiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Identifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.Input;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.KeyedInstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.ListAction;
import org.opendaylight.mdsal.binding.javav2.spec.base.Notification;
import org.opendaylight.mdsal.binding.javav2.spec.base.Output;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.RpcCallback;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeRoot;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.NotificationListener;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.mdsal.binding.javav2.spec.structural.TreeChildNode;


public final class BindingTypes {

    public static final ConcreteType ACTION = typeForClass(Action.class);
    public static final ConcreteType AUGMENTABLE = typeForClass(Augmentable.class);
    public static final ConcreteType AUGMENTATION = typeForClass(Augmentation.class);
    public static final ConcreteType IDENTIFIABLE_ITEM = typeForClass(IdentifiableItem.class);
    public static final ConcreteType IDENTIFIABLE = typeForClass(Identifiable.class);
    public static final ConcreteType IDENTIFIER = typeForClass(Identifier.class);
    public static final ConcreteType INPUT = typeForClass(Input.class);
    public static final ConcreteType INSTANCE_IDENTIFIER = typeForClass(InstanceIdentifier.class);
    public static final ConcreteType INSTANTIABLE = typeForClass(Instantiable.class);
    public static final ConcreteType ITEM = typeForClass(Item.class);
    public static final ConcreteType KEYED_INSTANCE_IDENTIFIER = typeForClass(KeyedInstanceIdentifier.class);
    public static final ConcreteType LIST_ACTION = typeForClass(ListAction.class);
    public static final ConcreteType NOTIFICATION = typeForClass(Notification.class);
    public static final ConcreteType NOTIFICATION_LISTENER = typeForClass(NotificationListener.class);
    public static final ConcreteType OUTPUT = typeForClass(Output.class);
    public static final ConcreteType RPC = typeForClass(Rpc.class);
    public static final ConcreteType RPC_CALLBACK = typeForClass(RpcCallback.class);
    public static final ConcreteType TREE_NODE = typeForClass(TreeNode.class);
    public static final ConcreteType TREE_ROOT = typeForClass(TreeRoot.class);
    public static final ConcreteType TREE_CHILD_NODE = typeForClass(TreeChildNode.class);

    private BindingTypes() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ParameterizedType augmentable(Type t) {
        return parameterizedTypeFor(AUGMENTABLE, t);
    }
}
