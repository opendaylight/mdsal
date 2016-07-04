/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.java.api.generator.renderers;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.mdsal.binding2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding2.txt.classTemplate;

public class ClassRenderer extends BaseRenderer {
    protected final GeneratedTransferObject genTO;
    protected final List<GeneratedProperty> properties;
    protected final List<GeneratedProperty> finalProperties;
    protected final List<GeneratedProperty> parentProperties;

    public ClassRenderer(final GeneratedTransferObject type) {
        super(type);
        genTO = type;
        finalProperties = new ArrayList<>();
        parentProperties = new ArrayList<>();
        properties = new ArrayList<>();
    }

    protected String generateAsInnerClass() {
        return generateBody(true);
    }

    @Override
    protected String body() {
        return generateBody(false);
    }

    protected String generateBody(boolean isInnerClass) {
        return classTemplate.render(genTO).body();
    }
}