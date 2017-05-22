package org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1;

import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.A.A1;
import org.opendaylight.mdsal.binding.javav2.spec.structural.AugmentationHolder;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Item;
import java.util.HashMap;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.concepts.Builder;
import com.google.common.collect.ClassToInstanceMap;
import java.util.Objects;
import java.util.Collections;
import java.util.Map;

/**
 * Class that builds {@link org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner} instances.
 * @see org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner
 */
public class A1InnerBuilder implements Builder <org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner> {

    private java.lang.String _leafAA1;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner>> augmentation = Collections.emptyMap();

    public A1InnerBuilder() {
    }

    public A1InnerBuilder (A1Inner base) {
        this._leafAA1 = base.getLeafAA1();
        if (base instanceof A1InnerImpl) {
            A1InnerImpl impl = (A1InnerImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner> casted =(AugmentationHolder<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }

    public java.lang.String getLeafAA1() {
        return _leafAA1;
    }

    @SuppressWarnings("unchecked")
    public <E extends Augmentation<? super A1Inner>> E getAugmentation (java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public A1InnerBuilder setLeafAA1(final String value) {
        this._leafAA1 = value;
        return this;
    }

    public A1InnerBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner>> augmentationType, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public A1InnerBuilder removeAugmentation (java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    @Override
    public A1Inner build() {
        return new A1InnerImpl(this);
    }

    private static final class A1InnerImpl implements A1Inner {
        @Override
        public java.lang.Class<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner> implementedInterface() {
            return org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner.class;
        }
        private  final java.lang.String _leafAA1;
        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner>> augmentation = Collections.emptyMap();
        private A1InnerImpl (A1InnerBuilder base) {
            this._leafAA1 = base.getLeafAA1();
            switch (base.augmentation.size()) {
                case 0: this.augmentation = Collections.emptyMap();
                break;
                case 1: final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner>> singletonMap(e.getKey(), e.getValue());
                break;
                default : this.augmentation = new HashMap<>(base.augmentation);
            }
        }
        @Override
        public java.lang.String getLeafAA1() {
            return _leafAA1;
        }
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<? super A1Inner>> E getAugmentation (java.lang.Class<E> augmentationType) {
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
            result = prime * result + Objects.hashCode(_leafAA1);
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
            if (!org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner.class.equals(((Instantiable)obj) .implementedInterface())) {
                return false;
            }
            org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner other = (org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner)obj;
            if (!Objects.equals(_leafAA1, other.getLeafAA1())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                A1InnerImpl otherImpl = (A1InnerImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.groupings.rev170515.data.a.A1.A1Inner>> e : augmentation.entrySet()) {
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
            java.lang.String name = "A1Inner [";
            java.lang.StringBuilder builder = new java.lang.StringBuilder(name);
            if (_leafAA1 != null) {
                builder.append("_leafAA1=");
                builder.append(_leafAA1);
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
        public Item<A1> treeIdentifier() {
            //TODO implement
            return null;
        }
        @Override
        public ClassToInstanceMap<Augmentation<? super A1Inner>> augments() {
            //TODO implement
            return null;
        }
    }

}
