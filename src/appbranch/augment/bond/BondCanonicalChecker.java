package appbranch.augment.bond;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import appbranch.augment.Augmentation;
import appbranch.canonical.CanonicalChecker;
import group.AtomDiscretePartitionRefiner;
import group.Permutation;
import group.PermutationGroup;

/**
 * Check a bond-wise augmentation of an IAtomContainer for canonicity. 
 * 
 * @author maclean
 *
 */
public class BondCanonicalChecker implements CanonicalChecker<IAtomContainer, BondExtension> {

    @Override
    public boolean isCanonical(Augmentation<IAtomContainer, BondExtension> augmentation) {
        IAtomContainer augmentedMolecule = augmentation.getBase();
        if (augmentedMolecule.getAtomCount() <= 2 || augmentedMolecule.getBondCount() == 0) {
            return true;
        }
        
        if (!inOrder(augmentedMolecule)) {
            return false;
        }
        
        BondExtension extension = augmentation.getExtension();
        IBond addedBond = getAddedBondIndex(augmentedMolecule, augmentation.getExtension());
        if (addedBond == null) {
//            System.out.println("disconnected is canonical");
            return true;    // disconnected atoms
        }
        
        AtomDiscretePartitionRefiner refiner = new AtomDiscretePartitionRefiner();
        PermutationGroup aut = refiner.getAutomorphismGroup(augmentedMolecule);
        Permutation labelling = refiner.getBest();
//        if (!labelling.isIdentity()) return false;
//        System.out.println("best = " + labelling);
        IBond canDelBond = getCanonicalDeletionBond(augmentedMolecule, labelling.invert());
        // note that (ai, aj) are just (extension.start, extension.end)
//        int ai = augmentedMolecule.getAtomNumber(addedBond.getAtom(0));
//        int aj = augmentedMolecule.getAtomNumber(addedBond.getAtom(1));
        int ai = extension.getIndexPair().getStart();
        int aj = extension.getIndexPair().getEnd();

        int bi = augmentedMolecule.getAtomNumber(canDelBond.getAtom(0));
        int bj = augmentedMolecule.getAtomNumber(canDelBond.getAtom(1));
//        System.out.println(ai + "," + aj + "," + bi + "," + bj);
        return inOrbit(ai, aj, bi, bj, aut);
    }
    
    private boolean inOrbit(int addedBondI, int addedBondJ, int canDelBondI, int canDelBondJ, PermutationGroup aut) {
        for (Permutation p : aut.all()) {
            int pi = p.get(addedBondI);
            int pj = p.get(addedBondJ);
            if ((pi == canDelBondI && pj == canDelBondJ) || (pj == canDelBondI && pi == canDelBondJ)) {
                return true;
            }
        }
        return false;
    }
    
    private IBond getCanonicalDeletionBond(IAtomContainer atomContainer, Permutation labelling) {
        String largest = null;
        IBond largestBond = null;
        for (IBond bond : atomContainer.bonds()) {
            int atomIndex0 = labelling.get(atomContainer.getAtomNumber(bond.getAtom(0)));
            int atomIndex1 = labelling.get(atomContainer.getAtomNumber(bond.getAtom(1)));
            String bondAsStr;
            if (atomIndex0 < atomIndex1) {
                bondAsStr = atomIndex0 + ":" + atomIndex1;
            } else {
                bondAsStr = atomIndex1 + ":" + atomIndex0;
            }
            if (largest == null || largest.compareTo(bondAsStr) < 0) {
                largest = bondAsStr;
                largestBond = bond;
            }
        }
        return largestBond;
    }
   
    private IBond getAddedBondIndex(IAtomContainer augmentedMolecule, BondExtension extension) {
        return augmentedMolecule.getBond(
                augmentedMolecule.getAtom(extension.getIndexPair().getStart()), 
                augmentedMolecule.getAtom(extension.getIndexPair().getEnd()));
    }

    private boolean inOrder(IAtomContainer augmentedMolecule) {
        String prev = null;
        for (IBond bond : augmentedMolecule.bonds()) {
            String current = toString(augmentedMolecule, bond);
            if (prev == null || prev.compareTo(current) < 0) {
                prev = current;
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    private String toString(IAtomContainer atomContainer, IBond bond) {
        return atomContainer.getAtomNumber(bond.getAtom(0)) + ":" 
                + atomContainer.getAtomNumber(bond.getAtom(1)); 
    }

}
