/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.codec.context.benchmark;

public abstract class DataContElem extends Elem {
    public DataContElem(String name, String value) {
        super(name, value);
    }

    public DataContElem(String name) {
        super(name);
    }

    @Override
    public StringBuilder build(StringBuilder target, int level) {
        target.append(IND.repeat(level));
        target.append(nameValue()).append(" {\n");
        if (elems() != null) {
            for (Elem elem : elems()) {
                elem.build(target, level + 1);
            }
        }
        target.append(IND.repeat(level));
        target.append("}\n");
        return target;
    }
}
