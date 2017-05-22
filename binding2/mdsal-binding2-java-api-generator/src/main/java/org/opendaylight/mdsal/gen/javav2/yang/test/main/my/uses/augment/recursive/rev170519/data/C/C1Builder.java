package org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C;

import org.opendaylight.mdsal.binding.javav2.spec.structural.AugmentationHolder;
import org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.grp.C;
import java.util.HashMap;
import org.opendaylight.yangtools.concepts.Builder;
import com.google.common.collect.ClassToInstanceMap;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import java.util.Objects;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner;

/**
 * Class that builds {@link org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1} instances.
 * @see org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1
 */
public class C1Builder implements Builder <org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1> {

    private C1Inner _c1Inner;

    private java.lang.String _leafCC1;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1>> augmentation = Collections.emptyMap();

    public C1Builder() {
    }

    public C1Builder (C1 base) {
        this._c1Inner = base.getC1Inner();
        this._leafCC1 = base.getLeafCC1();
        if (base instanceof C1Impl) {
            C1Impl impl = (C1Impl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1> casted =(AugmentationHolder<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }

    public C1Inner getC1Inner() {
        return _c1Inner;
    }

    public java.lang.String getLeafCC1() {
        return _leafCC1;
    }

    @SuppressWarnings("unchecked")
    public <E extends Augmentation<? super C1>> E getAugmentation (java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public C1Builder setC1Inner(final C1Inner value) {
        this._c1Inner = value;
        return this;
    }

    public C1Builder setLeafCC1(final String value) {
        this._leafCC1 = value;
        return this;
    }

    public C1Builder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1>> augmentationType, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public C1Builder removeAugmentation (java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    @Override
    public C1 build() {
        return new C1Impl(this);
    }

    private static final class C1Impl implements C1 {
        @Override
        public java.lang.Class<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1> implementedInterface() {
            return org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1.class;
        }
        private  final C1Inner _c1Inner;
        private  final java.lang.String _leafCC1;
        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1>> augmentation = Collections.emptyMap();
        private C1Impl (C1Builder base) {
            this._c1Inner = base.getC1Inner();
            this._leafCC1 = base.getLeafCC1();
            switch (base.augmentation.size()) {
                case 0: this.augmentation = Collections.emptyMap();
                break;
                case 1: final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1>> singletonMap(e.getKey(), e.getValue());
                break;
                default : this.augmentation = new HashMap<>(base.augmentation);
            }
        }
        @Override
        public C1Inner getC1Inner() {
            return _c1Inner;
        }
        @Override
        public java.lang.String getLeafCC1() {
            return _leafCC1;
        }
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<? super C1>> E getAugmentation (java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
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
            result = prime * result + Objects.hashCode(_c1Inner);
            result = prime * result + Objects.hashCode(_leafCC1);
            result = prime * result + Objects.hashCode(augmentation);
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
            if (!org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1.class.equals(((Instantiable)obj) .implementedInterface())) {
                return false;
            }
            org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1 other = (org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1)obj;
            if (!Objects.equals(_c1Inner, other.getC1Inner())) {
                return false;
            }
            if (!Objects.equals(_leafCC1, other.getLeafCC1())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                C1Impl otherImpl = (C1Impl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1>> e : augmentation.entrySet()) {
                    if (!e.getValue().equals(other.getAugmentation(e.getKey()))) {
                        return false;
                    }
                }
                // .. and give the other one the chance to do the same
                if (!obj.equals(this)) {
                    return false;
                }
            }
            return true;
        }
        @Override
        public java.lang.String toString() {
            java.lang.String name = "C1 [";
            java.lang.StringBuilder builder = new java.lang.StringBuilder(name);
            if (_c1Inner != null) {
                builder.append("_c1Inner=");
                builder.append(_c1Inner);
                builder.append(", ");
            }
            if (_leafCC1 != null) {
                builder.append("_leafCC1=");
                builder.append(_leafCC1);
            }
            final int builderLength = builder.length();
            final int builderAdditionalLength = builder.substring(name.length(), builderLength).length();
            if (builderAdditionalLength > 2 && !builder.substring(builderLength - 2, builderLength).equals(", ")) {
                builder.append(", ");
            }
            builder.append("augmentation=");
            builder.append(augmentation.values());
            return builder.append(']').toString();
        }
        @Override
        public Item<C> treeIdentifier() {
            //TODO implement
            return null;
        }
        @Override
        public ClassToInstanceMap<Augmentation<? super C1>> augments() {
            //TODO implement
            return null;
        }
    }

}
