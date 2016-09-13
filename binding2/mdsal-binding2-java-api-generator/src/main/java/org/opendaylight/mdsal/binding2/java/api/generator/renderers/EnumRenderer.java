/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.java.api.generator.renderers;

import org.opendaylight.mdsal.binding2.model.api.Enumeration;
import org.opendaylight.mdsal.binding2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding2.txt.enumTemplate;

public class EnumRenderer extends BaseRenderer {
    private final Enumeration enums;

    public EnumRenderer(final Enumeration type) {
        super(((GeneratedType) type));
        enums = type;
    }

    @Override
    protected String body() {
        String importedName = importedName(String.class);
        return enumTemplate.render(enums, importedName).body();
    }
}