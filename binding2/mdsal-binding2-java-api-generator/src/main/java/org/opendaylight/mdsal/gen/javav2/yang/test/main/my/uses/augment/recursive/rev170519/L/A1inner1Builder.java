package org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.L;

import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.concepts.Builder;
import com.google.common.collect.ClassToInstanceMap;
import java.util.Objects;
import org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.B.B1;

/**
 * Class that builds {@link org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.L.A1inner1} instances.
 * @see org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.L.A1inner1
 */
public class A1inner1Builder implements Builder <org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.L.A1inner1> {

    private B1 _b1;

    private java.lang.String _dAugA1;

    public A1inner1Builder() {
    }

    public A1inner1Builder( org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.grp.B arg) {
        this._b1 = arg.getB1();
    }

    public A1inner1Builder (A1inner1 base) {
        this._b1 = base.getB1();
        this._dAugA1 = base.getDAugA1();
    }

    /**
     *Set fields from given grouping argument. Valid argument is instance of one of following types:
     * <ul>
     * <li>org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.grp.B</li>
     * </ul>
     *
     * @param arg grouping object
     * @throws IllegalArgumentException if given argument is none of valid types
     */
    public void fieldsFrom(TreeNode arg) {
        boolean isValidArg = false;
        if (arg instanceof org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.grp.B) {
            this._dAugA1 = ((org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.grp.B)arg).getDAugA1();
            isValidArg = true;
        }
        if (!isValidArg) {
            throw new IllegalArgumentException( "expected one of: [org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.grp.B] \n" + "but was: " + arg );
        }
    }

    public B1 getB1() {
        return _b1;
    }

    public java.lang.String getDAugA1() {
        return _dAugA1;
    }

    public A1inner1Builder setDAugA1(final String value) {
        this._dAugA1 = value;
        return this;
    }

    public A1inner1Builder setB1(final B1 value) {
        this._b1 = value;
        return this;
    }

    @Override
    public A1inner1 build() {
        return new A1inner1Impl(this);
    }

    private static final class A1inner1Impl implements A1inner1 {
        @Override
        public java.lang.Class<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.L.A1inner1> implementedInterface() {
            return org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.L.A1inner1.class;
        }
        private  final B1 _b1;
        private  final java.lang.String _dAugA1;
        private A1inner1Impl (A1inner1Builder base) {
            this._b1 = base.getB1();
            this._dAugA1 = base.getDAugA1();
        }
        @Override
        public B1 getB1() {
            return _b1;
        }
        @Override
        public java.lang.String getDAugA1() {
            return _dAugA1;
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
            result = prime * result + Objects.hashCode(_b1);
            result = prime * result + Objects.hashCode(_dAugA1);
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
            if (!org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.L.A1inner1.class.equals(((Instantiable)obj) .implementedInterface())) {
                return false;
            }
            org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.L.A1inner1 other = (org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.L.A1inner1)obj;
            if (!Objects.equals(_dAugA1, other.getDAugA1())) {
                return false;
            }
            if (!Objects.equals(_b1, other.getB1())) {
                return false;
            }
            return true;
        }
        @Override
        public java.lang.String toString() {
            java.lang.String name = "A1inner1 [";
            java.lang.StringBuilder builder = new java.lang.StringBuilder(name);
            if (_b1 != null) {
                builder.append("_b1=");
                builder.append(_b1);
                builder.append(", ");
            }
            if (_dAugA1 != null) {
                builder.append("_dAugA1=");
                builder.append(_dAugA1);
            }
            return builder.append(']').toString();
        }
    }

}
