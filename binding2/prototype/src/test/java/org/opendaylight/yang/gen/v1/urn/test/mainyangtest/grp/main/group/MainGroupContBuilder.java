package org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import java.util.HashMap;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContChoice;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListOrdered;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.main.group.cont.MainGroupContListUnordered;
import org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.second.group.SecondGroupCont;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.TreeNode;
import java.util.Objects;
import java.util.List;
import java.util.Collections;
import java.util.Map;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont} instances.
 *
 * @see org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont
 *
 */
public class MainGroupContBuilder implements Builder <org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont> {

    private java.lang.String _leafRefTest;
    private MainGroupContChoice _mainGroupContChoice;
    private java.lang.String _mainGroupContLeaf;
    private List<MainGroupContListOrdered> _mainGroupContListOrdered;
    private List<MainGroupContListUnordered> _mainGroupContListUnordered;
    private SecondGroupCont _secondGroupCont;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont>> augmentation = Collections.emptyMap();

    public MainGroupContBuilder() {
    }
    public MainGroupContBuilder(org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.SecondGroup arg) {
        this._secondGroupCont = arg.getSecondGroupCont();
    }

    public MainGroupContBuilder(MainGroupCont base) {
        this._leafRefTest = base.getLeafRefTest();
        this._mainGroupContChoice = base.getMainGroupContChoice();
        this._mainGroupContLeaf = base.getMainGroupContLeaf();
        this._mainGroupContListOrdered = base.getMainGroupContListOrdered();
        this._mainGroupContListUnordered = base.getMainGroupContListUnordered();
        this._secondGroupCont = base.getSecondGroupCont();
        if (base instanceof MainGroupContImpl) {
            MainGroupContImpl impl = (MainGroupContImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont> casted =(AugmentationHolder<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }

    /**
     *Set fields from given grouping argument. Valid argument is instance of one of following types:
     * <ul>
     * <li>org.opendaylight.yang.gen.v1.urn.test.mainyangtest.rev160101.SecondGroup</li>
     * </ul>
     *
     * @param arg grouping object
     * @throws IllegalArgumentException if given argument is none of valid types
    */
    public void fieldsFrom(TreeNode arg) {
        boolean isValidArg = false;
        if (arg instanceof org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.SecondGroup) {
            this._secondGroupCont = ((org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.SecondGroup)arg).getSecondGroupCont();
            isValidArg = true;
        }
        if (!isValidArg) {
            throw new IllegalArgumentException(
              "expected one of: [org.opendaylight.yang.gen.v1.urn.test.mainyangtest.rev160101.SecondGroup] \n" +
              "but was: " + arg
            );
        }
    }

    public java.lang.String getLeafRefTest() {
        return _leafRefTest;
    }

    public MainGroupContChoice getMainGroupContChoice() {
        return _mainGroupContChoice;
    }

    public java.lang.String getMainGroupContLeaf() {
        return _mainGroupContLeaf;
    }

    public List<MainGroupContListOrdered> getMainGroupContListOrdered() {
        return _mainGroupContListOrdered;
    }

    public List<MainGroupContListUnordered> getMainGroupContListUnordered() {
        return _mainGroupContListUnordered;
    }

    public SecondGroupCont getSecondGroupCont() {
        return _secondGroupCont;
    }

    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }


    public MainGroupContBuilder setLeafRefTest(final java.lang.String value) {
        this._leafRefTest = value;
        return this;
    }


    public MainGroupContBuilder setMainGroupContChoice(final MainGroupContChoice value) {
        this._mainGroupContChoice = value;
        return this;
    }


    public MainGroupContBuilder setMainGroupContLeaf(final java.lang.String value) {
        this._mainGroupContLeaf = value;
        return this;
    }


    public MainGroupContBuilder setMainGroupContListOrdered(final List<MainGroupContListOrdered> value) {
        this._mainGroupContListOrdered = value;
        return this;
    }


    public MainGroupContBuilder setMainGroupContListUnordered(final List<MainGroupContListUnordered> value) {
        this._mainGroupContListUnordered = value;
        return this;
    }


    public MainGroupContBuilder setSecondGroupCont(final SecondGroupCont value) {
        this._secondGroupCont = value;
        return this;
    }

    public MainGroupContBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }

        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }

        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public MainGroupContBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    public MainGroupCont build() {
        return new MainGroupContImpl(this);
    }

    private static final class MainGroupContImpl implements MainGroupCont {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont> implementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont.class;
        }

        private final java.lang.String _leafRefTest;
        private final MainGroupContChoice _mainGroupContChoice;
        private final java.lang.String _mainGroupContLeaf;
        private final List<MainGroupContListOrdered> _mainGroupContListOrdered;
        private final List<MainGroupContListUnordered> _mainGroupContListUnordered;
        private final SecondGroupCont _secondGroupCont;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont>> augmentation = Collections.emptyMap();

        private MainGroupContImpl(MainGroupContBuilder base) {
            this._leafRefTest = base.getLeafRefTest();
            this._mainGroupContChoice = base.getMainGroupContChoice();
            this._mainGroupContLeaf = base.getMainGroupContLeaf();
            this._mainGroupContListOrdered = base.getMainGroupContListOrdered();
            this._mainGroupContListUnordered = base.getMainGroupContListUnordered();
            this._secondGroupCont = base.getSecondGroupCont();
            switch (base.augmentation.size()) {
            case 0:
                this.augmentation = Collections.emptyMap();
                break;
            case 1:
                final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont>>singletonMap(e.getKey(), e.getValue());
                break;
            default :
                this.augmentation = new HashMap<>(base.augmentation);
            }
        }

        @Override
        public java.lang.String getLeafRefTest() {
            return _leafRefTest;
        }

        @Override
        public MainGroupContChoice getMainGroupContChoice() {
            return _mainGroupContChoice;
        }

        @Override
        public java.lang.String getMainGroupContLeaf() {
            return _mainGroupContLeaf;
        }

        @Override
        public List<MainGroupContListOrdered> getMainGroupContListOrdered() {
            return _mainGroupContListOrdered;
        }

        @Override
        public List<MainGroupContListUnordered> getMainGroupContListUnordered() {
            return _mainGroupContListUnordered;
        }

        @Override
        public SecondGroupCont getSecondGroupCont() {
            return _secondGroupCont;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont>> E getAugmentation(java.lang.Class<E> augmentationType) {
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
            result = prime * result + Objects.hashCode(_leafRefTest);
            result = prime * result + Objects.hashCode(_mainGroupContChoice);
            result = prime * result + Objects.hashCode(_mainGroupContLeaf);
            result = prime * result + Objects.hashCode(_mainGroupContListOrdered);
            result = prime * result + Objects.hashCode(_mainGroupContListUnordered);
            result = prime * result + Objects.hashCode(_secondGroupCont);
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
            if (!org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont.class.equals(((TreeNode)obj).implementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont other = (org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont)obj;
            if (!Objects.equals(_leafRefTest, other.getLeafRefTest())) {
                return false;
            }
            if (!Objects.equals(_mainGroupContChoice, other.getMainGroupContChoice())) {
                return false;
            }
            if (!Objects.equals(_mainGroupContLeaf, other.getMainGroupContLeaf())) {
                return false;
            }
            if (!Objects.equals(_mainGroupContListOrdered, other.getMainGroupContListOrdered())) {
                return false;
            }
            if (!Objects.equals(_mainGroupContListUnordered, other.getMainGroupContListUnordered())) {
                return false;
            }
            if (!Objects.equals(_secondGroupCont, other.getSecondGroupCont())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                MainGroupContImpl otherImpl = (MainGroupContImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont>>, Augmentation<org.opendaylight.yang.gen.v1.urn.test.mainyangtest.grp.main.group.MainGroupCont>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("MainGroupCont [");
            boolean first = true;

            if (_leafRefTest != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_leafRefTest=");
                builder.append(_leafRefTest);
             }
            if (_mainGroupContChoice != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_mainGroupContChoice=");
                builder.append(_mainGroupContChoice);
             }
            if (_mainGroupContLeaf != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_mainGroupContLeaf=");
                builder.append(_mainGroupContLeaf);
             }
            if (_mainGroupContListOrdered != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_mainGroupContListOrdered=");
                builder.append(_mainGroupContListOrdered);
             }
            if (_mainGroupContListUnordered != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_mainGroupContListUnordered=");
                builder.append(_mainGroupContListUnordered);
             }
            if (_secondGroupCont != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_secondGroupCont=");
                builder.append(_secondGroupCont);
             }
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("augmentation=");
            builder.append(augmentation.values());
            return builder.append(']').toString();
        }
    }

}
