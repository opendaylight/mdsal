/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.dom.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;

@Beta
public abstract class DOMQueryPredicate implements Immutable {
    public static final class Exists extends DOMQueryPredicate {
        // FIXME: singleton?

    }

    public static final class ValueEquals<T> extends DOMQueryPredicate {
        private final @NonNull T value;

        public ValueEquals(final T value) {
            this.value = requireNonNull(value);
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("value", value);
        }
    }

    abstract static class AbstractStringDOMQueryPredicate extends DOMQueryPredicate {
        private final String str;

        AbstractStringDOMQueryPredicate(final String str) {
            this.str = requireNonNull(str);
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("str", str);
        }
    }

    public static final class StartsWith extends AbstractStringDOMQueryPredicate {
        public StartsWith(final String str) {
            super(str);
        }
    }

    public static final class EndsWith extends AbstractStringDOMQueryPredicate {
        public EndsWith(final String str) {
            super(str);
        }
    }

    public static final class Contains extends AbstractStringDOMQueryPredicate {
        public Contains(final String str) {
            super(str);
        }
    }

    public static final class MatchesPattern extends DOMQueryPredicate {
        private final Pattern pattern;

        public MatchesPattern(final Pattern pattern) {
            this.pattern = requireNonNull(pattern);
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("pattern", pattern);
        }
    }

    DOMQueryPredicate() {
        // Hidden on purpose
    }

    @Override
    public String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper;
    }
}
