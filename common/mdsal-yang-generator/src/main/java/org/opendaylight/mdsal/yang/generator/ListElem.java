/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yang.generator;

public class ListElem extends DataContElem {
    public ListElem(String name) {
        super("list", name);
    }

    public ListElem(String name, String key) {
        this(name);
        with(new TerminalElem("key", key));
    }
}
