package org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.l.a1_inner.B1_inner;

import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.concepts.Builder;
import com.google.common.collect.ClassToInstanceMap;
import java.util.Objects;

/**
 * Class that builds {@link org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.l.a1_inner.B1_inner.C1inner1} instances.
 * @see org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.l.a1_inner.B1_inner.C1inner1
 */
public class C1inner1Builder implements Builder <org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.l.a1_inner.B1_inner.C1inner1> {

    private java.lang.String _dAugC1Inner;

    public C1inner1Builder() {
    }

    public C1inner1Builder (C1inner1 base) {
        this._dAugC1Inner = base.getDAugC1Inner();
    }

    public java.lang.String getDAugC1Inner() {
        return _dAugC1Inner;
    }

    public C1inner1Builder setDAugC1Inner(final String value) {
        this._dAugC1Inner = value;
        return this;
    }

    @Override
    public C1inner1 build() {
        return new C1inner1Impl(this);
    }

    private static final class C1inner1Impl implements C1inner1 {
        @Override
        public java.lang.Class<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.l.a1_inner.B1_inner.C1inner1> implementedInterface() {
            return org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.l.a1_inner.B1_inner.C1inner1.class;
        }
        private  final java.lang.String _dAugC1Inner;
        private C1inner1Impl (C1inner1Builder base) {
            this._dAugC1Inner = base.getDAugC1Inner();
        }
        @Override
        public java.lang.String getDAugC1Inner() {
            return _dAugC1Inner;
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
            result = prime * result + Objects.hashCode(_dAugC1Inner);
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
            if (!org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.l.a1_inner.B1_inner.C1inner1.class.equals(((Instantiable)obj) .implementedInterface())) {
                return false;
            }
            org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.l.a1_inner.B1_inner.C1inner1 other = (org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.l.a1_inner.B1_inner.C1inner1)obj;
            if (!Objects.equals(_dAugC1Inner, other.getDAugC1Inner())) {
                return false;
            }
            return true;
        }
        @Override
        public java.lang.String toString() {
            java.lang.String name = "C1inner1 [";
            java.lang.StringBuilder builder = new java.lang.StringBuilder(name);
            if (_dAugC1Inner != null) {
                builder.append("_dAugC1Inner=");
                builder.append(_dAugC1Inner);
            }
            return builder.append(']').toString();
        }
    }

}
