/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yang.generator;

public class ModuleElem extends DataContElem {
    private final StringBuilder output = new StringBuilder();

    private String name;
    private String prefix;
    private String namespace;
    private String revision;

    public String build() {
        return build(output).toString();
    }

    public ModuleElem(String name) {
        super("module", name);
    }

    public ModuleElem(String name, String namespace, String prefix, String revision) {
        this(name);
        with(new TerminalElem("namespace", namespace));
        with(new TerminalElem("prefix", prefix));
        with(new TerminalElem("revision", revision));
    }
}
