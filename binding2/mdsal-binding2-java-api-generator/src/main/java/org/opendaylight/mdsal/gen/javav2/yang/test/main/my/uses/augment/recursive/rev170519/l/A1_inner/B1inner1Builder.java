package org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.l.A1_inner;

import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.concepts.Builder;
import com.google.common.collect.ClassToInstanceMap;
import java.util.Objects;
import org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1;

/**
 * Class that builds {@link org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.l.A1_inner.B1inner1} instances.
 * @see org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.l.A1_inner.B1inner1
 */
public class B1inner1Builder implements Builder <org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.l.A1_inner.B1inner1> {

    private C1 _c1;

    private java.lang.String _dAugB1Inner;

    public B1inner1Builder() {
    }

    public B1inner1Builder( org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.grp.C arg) {
        this._c1 = arg.getC1();
    }

    public B1inner1Builder (B1inner1 base) {
        this._c1 = base.getC1();
        this._dAugB1Inner = base.getDAugB1Inner();
    }

    /**
     *Set fields from given grouping argument. Valid argument is instance of one of following types:
     * <ul>
     * <li>org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.grp.C</li>
     * </ul>
     *
     * @param arg grouping object
     * @throws IllegalArgumentException if given argument is none of valid types
     */
    public void fieldsFrom(TreeNode arg) {
        boolean isValidArg = false;
        if (arg instanceof org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.grp.C) {
            this._dAugB1Inner = ((org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.grp.C)arg).getDAugB1Inner();
            isValidArg = true;
        }
        if (!isValidArg) {
            throw new IllegalArgumentException( "expected one of: [org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.grp.C] \n" + "but was: " + arg );
        }
    }

    public C1 getC1() {
        return _c1;
    }

    public java.lang.String getDAugB1Inner() {
        return _dAugB1Inner;
    }

    public B1inner1Builder setC1(final C1 value) {
        this._c1 = value;
        return this;
    }

    public B1inner1Builder setDAugB1Inner(final String value) {
        this._dAugB1Inner = value;
        return this;
    }

    @Override
    public B1inner1 build() {
        return new B1inner1Impl(this);
    }

    private static final class B1inner1Impl implements B1inner1 {
        @Override
        public java.lang.Class<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.l.A1_inner.B1inner1> implementedInterface() {
            return org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.l.A1_inner.B1inner1.class;
        }
        private  final C1 _c1;
        private  final java.lang.String _dAugB1Inner;
        private B1inner1Impl (B1inner1Builder base) {
            this._c1 = base.getC1();
            this._dAugB1Inner = base.getDAugB1Inner();
        }
        @Override
        public C1 getC1() {
            return _c1;
        }
        @Override
        public java.lang.String getDAugB1Inner() {
            return _dAugB1Inner;
        }
        private int hash = 0;
        private volatile boolean hashValid = false;
        @Override
        public int hashCode() {
            if (hashValid) {
                return hash;
            }
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(_dAugB1Inner);
            result = prime * result + Objects.hashCode(_c1);
            hash = result;
            hashValid = true;
            return result;
        }
        @Override
        public boolean equals(java.lang.Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TreeNode)) {
                return false;
            }
            if (!(obj instanceof Instantiable)) {
                return false;
            }
            if (!org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.l.A1_inner.B1inner1.class.equals(((Instantiable)obj) .implementedInterface())) {
                return false;
            }
            org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.l.A1_inner.B1inner1 other = (org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.l.A1_inner.B1inner1)obj;
            if (!Objects.equals(_dAugB1Inner, other.getDAugB1Inner())) {
                return false;
            }
            if (!Objects.equals(_c1, other.getC1())) {
                return false;
            }
            return true;
        }
        @Override
        public java.lang.String toString() {
            java.lang.String name = "B1inner1 [";
            java.lang.StringBuilder builder = new java.lang.StringBuilder(name);
            if (_dAugB1Inner != null) {
                builder.append("_dAugB1Inner=");
                builder.append(_dAugB1Inner);
            }
            if (_c1 != null) {
                builder.append("_c1=");
                builder.append(_c1);
                builder.append(", ");
            }
            return builder.append(']').toString();
        }
    }

}
