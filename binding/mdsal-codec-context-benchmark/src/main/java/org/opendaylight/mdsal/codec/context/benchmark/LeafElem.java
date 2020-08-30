/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.codec.context.benchmark;

public class LeafElem extends DataContElem {
    public LeafElem(final String name, final String type) {
        this(name);
        with(new TerminalElem("type", type));
    }

    public LeafElem(final String name) {
        super("leaf", name);
    }

    public LeafElem() {
        super("leaf");
    }

    public static LeafElem stringLeaf(String name) {
        return new LeafElem(name, "string");
    }
}
