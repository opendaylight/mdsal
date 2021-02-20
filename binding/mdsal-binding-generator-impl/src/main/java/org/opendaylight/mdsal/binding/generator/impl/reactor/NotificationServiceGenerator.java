/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.util.BindingTypes;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;

final class NotificationServiceGenerator extends AbstractImplicitGenerator {
    private final List<NotificationGenerator> notifs;

    NotificationServiceGenerator(final ModuleGenerator parent, final List<NotificationGenerator> notifs) {
        super(parent);
        this.notifs = requireNonNull(notifs);
    }

    @Override
    String suffix() {
        return BindingMapping.NOTIFICATION_LISTENER_SUFFIX;
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());
        builder.addImplementsType(BindingTypes.NOTIFICATION_LISTENER);

        for (NotificationGenerator gen : notifs) {
            final MethodSignatureBuilder notificationMethod = builder.addMethod("on" + gen.assignedName())
                .setAccessModifier(AccessModifier.PUBLIC)
                .addParameter(gen.getType(builderFactory), "notification")
                .setReturnType(Types.primitiveVoidType());

            // FIXME: finish this up this
            //          annotateDeprecatedIfNecessary(notification, notificationMethod);
            //          if (notification.getStatus().equals(Status.OBSOLETE)) {
            //              notificationMethod.setDefault(true);
            //          }
            //          addComment(notificationMethod, notification);
        }

        return builder.build();
    }
}
