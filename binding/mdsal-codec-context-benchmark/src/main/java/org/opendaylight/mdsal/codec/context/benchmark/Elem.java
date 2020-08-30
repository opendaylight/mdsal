/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.codec.context.benchmark;

import java.util.ArrayList;
import java.util.List;

public abstract class Elem {
    private List<Elem> elements;
    private String name;
    private String value;

    public static final String WS = " ";
    public static final String IND = WS + WS;


    public Elem(String name) {
        this.name = name;
    }

    public Elem(String name, String value) {
        this(name);
        this.value = value;
    }

    public StringBuilder build(StringBuilder target) {
        return build(target, 0);
    }

    public abstract StringBuilder build(StringBuilder target, int level);

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    public String nameValue() {
        return name() + WS + value();
    }

    public List<Elem> elems() {
        return elements;
    }

    public Elem with(final Elem elem) {
        if (elements == null) {
            elements = new ArrayList<>();
        }
        elements.add(elem);
        return this;
    }
}
