/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.java.api.generator.renderers;

import org.opendaylight.mdsal.binding2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding2.txt.interfaceTemplate;

public class InterfaceRenderer extends BaseRenderer {
    /**
     * Creates the instance of this class which is used for generating the interface file source
     * code from <code>type</code>.

     * @param type generated type
     */
    public InterfaceRenderer(final GeneratedType type) {
        super(type);
        if (type == null) {
            throw new IllegalArgumentException("Generated type reference cannot be NULL!");
        }
    }

    @Override
    protected String body() {
//        imported name insert another list
//        enum template call
//        constant importedname
        return interfaceTemplate.render(type).body();
    }
}