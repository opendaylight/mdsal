/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.NotificationRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

@Beta
public final class DefaultNotificationRuntimeType extends AbstractCompositeRuntimeType<NotificationEffectiveStatement>
        implements NotificationRuntimeType {
    public DefaultNotificationRuntimeType(final GeneratedType bindingType, final NotificationEffectiveStatement schema,
            final Map<RuntimeType, EffectiveStatement<?, ?>> children) {
        super(bindingType, schema, children);
    }
}
