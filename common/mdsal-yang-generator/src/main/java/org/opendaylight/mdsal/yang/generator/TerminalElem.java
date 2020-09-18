/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yang.generator;

public class TerminalElem extends Elem {
    public TerminalElem(String name, String value) {
        super(name, value);
    }

    @Override
    public StringBuilder build(StringBuilder target) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder build(StringBuilder target, int level) {
        return target.append(IND.repeat(level)).append(nameValue()).append(WS).append(";\n");
    }
}
