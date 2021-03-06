package augment.atom;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond.Order;

import augment.Augmentation;
import augment.constraints.ElementConstraints;

/**
 * An augmentation of an atom container by atom.
 * 
 * @author maclean
 *
 */
public class AtomAugmentation implements Augmentation<IAtomContainer> {
    
    private final IAtomContainer augmentedMolecule;
    
    private final AtomExtension atomExtension;
    
    private final ElementConstraints elementConstraints;
    
   
    /**
     * Construct the initial state.
     * 
     * @param initialAtom
     */
    public AtomAugmentation(IAtom initialAtom, ElementConstraints elementConstraints) {
        augmentedMolecule = initialAtom.getBuilder().newInstance(IAtomContainer.class);
        augmentedMolecule.addAtom(initialAtom);
        
        // XXX TODO - wrong way round!
        this.atomExtension = new AtomExtension(initialAtom.getSymbol(), new int[] {});
        this.elementConstraints = elementConstraints;
    }
    
    public AtomAugmentation(IAtomContainer initialContainer, ElementConstraints elementConstraints) {
        augmentedMolecule = initialContainer;   // TODO : could clone...
        this.atomExtension = null;  // XXX!
        this.elementConstraints = elementConstraints;
    }
    
    /**
     * Make an augmentation from a parent, an atom, and a set of bonds. The augmentation
     * array is a list like {0, 1, 0, 2} which means add a single bond to atom 1 and 
     * a double to atom 3, connecting both to the new atom. 
     *  
     * @param parent the atom container to augment
     * @param atomToAdd the additional atom
     * @param augmentation a list of bond orders to augment
     */
    public AtomAugmentation(IAtomContainer parent, IAtom atomToAdd, int[] bondOrders, ElementConstraints elementConstraints) {
        this.atomExtension = new AtomExtension(atomToAdd.getSymbol(), bondOrders);
        this.augmentedMolecule = make(parent, atomToAdd, bondOrders);
        this.elementConstraints = elementConstraints;
    }
    
    public IAtomContainer getAugmentedObject() {
        return augmentedMolecule;
    }
    
    public ElementConstraints getConstraints() {
        return elementConstraints;
    }

    private IAtomContainer make(IAtomContainer parent, IAtom atomToAdd, int[] augmentation) {
        try {
            int lastIndex = augmentation.length;
            IAtomContainer child = (IAtomContainer) parent.clone();
            child.addAtom(atomToAdd);

            for (int index = 0; index < augmentation.length; index++) {
                int value = augmentation[index];
                if (value > 0) {
                    Order order;
                    switch (value) {
                        case 1: order = Order.SINGLE; break;
                        case 2: order = Order.DOUBLE; break;
                        case 3: order = Order.TRIPLE; break;
                        default: order = Order.SINGLE;
                    }
                    child.addBond(index, lastIndex, order);
                    IAtom partner = child.getAtom(index);
                    Integer hCount = partner.getImplicitHydrogenCount();
                    int partnerCount = (hCount == null)? 0 : hCount;
                    partner.setImplicitHydrogenCount(partnerCount - value);
                }
            }
            return child;
        } catch (CloneNotSupportedException cnse) {
            // TODO
            return null;
        }
    }

    public AtomExtension getExtension() {
        return atomExtension;
    }
    
    public String toString() {
        return io.AtomContainerPrinter.toString(augmentedMolecule)
                + " -> " + atomExtension
                + " " + elementConstraints;
    }
}
