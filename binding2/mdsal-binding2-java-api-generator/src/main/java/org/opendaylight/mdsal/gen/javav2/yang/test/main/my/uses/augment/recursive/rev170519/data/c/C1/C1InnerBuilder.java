package org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1;

import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
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
import org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.C.C1;

/**
 * Class that builds {@link org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner} instances.
 * @see org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner
 */
public class C1InnerBuilder implements Builder <org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner> {

    private java.lang.String _leafCC1;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner>> augmentation = Collections.emptyMap();

    public C1InnerBuilder() {
    }

    public C1InnerBuilder (C1Inner base) {
        this._leafCC1 = base.getLeafCC1();
        if (base instanceof C1InnerImpl) {
            C1InnerImpl impl = (C1InnerImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner> casted =(AugmentationHolder<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }

    public java.lang.String getLeafCC1() {
        return _leafCC1;
    }

    @SuppressWarnings("unchecked")
    public <E extends Augmentation<? super C1Inner>> E getAugmentation (java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public C1InnerBuilder setLeafCC1(final String value) {
        this._leafCC1 = value;
        return this;
    }

    public C1InnerBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner>> augmentationType, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public C1InnerBuilder removeAugmentation (java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    @Override
    public C1Inner build() {
        return new C1InnerImpl(this);
    }

    private static final class C1InnerImpl implements C1Inner {
        @Override
        public java.lang.Class<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner> implementedInterface() {
            return org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner.class;
        }
        private  final java.lang.String _leafCC1;
        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner>> augmentation = Collections.emptyMap();
        private C1InnerImpl (C1InnerBuilder base) {
            this._leafCC1 = base.getLeafCC1();
            switch (base.augmentation.size()) {
                case 0: this.augmentation = Collections.emptyMap();
                break;
                case 1: final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner>> singletonMap(e.getKey(), e.getValue());
                break;
                default : this.augmentation = new HashMap<>(base.augmentation);
            }
        }
        @Override
        public java.lang.String getLeafCC1() {
            return _leafCC1;
        }
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<? super C1Inner>> E getAugmentation (java.lang.Class<E> augmentationType) {
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
            if (!org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner.class.equals(((Instantiable)obj) .implementedInterface())) {
                return false;
            }
            org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner other = (org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner)obj;
            if (!Objects.equals(_leafCC1, other.getLeafCC1())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                C1InnerImpl otherImpl = (C1InnerImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner>>, Augmentation<org.opendaylight.mdsal.gen.javav2.yang.test.main.my.uses.augment.recursive.rev170519.data.c.C1.C1Inner>> e : augmentation.entrySet()) {
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
            java.lang.String name = "C1Inner [";
            java.lang.StringBuilder builder = new java.lang.StringBuilder(name);
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
        public Item<C1> treeIdentifier() {
            //TODO implement
            return null;
        }
        @Override
        public ClassToInstanceMap<Augmentation<? super C1Inner>> augments() {
            //TODO implement
            return null;
        }
    }

}
