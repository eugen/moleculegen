package augment.atom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openscience.cdk.group.AtomContainerDiscretePartitionRefiner;
import org.openscience.cdk.group.PartitionRefinement;
import org.openscience.cdk.group.Permutation;
import org.openscience.cdk.group.PermutationGroup;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

import augment.Augmentor;
import augment.chem.SaturationCalculator;
import augment.constraints.ElementConstraints;
//import group.Permutation;
//import group.PermutationGroup;
//import group.molecule.AtomDiscretePartitionRefiner;

public class AtomAugmentor implements Augmentor<AtomAugmentation> {
    
    private static final long serialVersionUID = -1795216862782671835L;

    public static IChemObjectBuilder getBuilder() {
        return SilentChemObjectBuilder.getInstance();
    }
    
    /**
     * The elements (in order) used to make molecules for this run.
     */
    private List<String> elementSymbols;
    
    private SaturationCalculator saturationCalculator = new SaturationCalculator();
    
    public AtomAugmentor(String elementString) {
        elementSymbols = new ArrayList<String>();
        for (int i = 0; i < elementString.length(); i++) {
            elementSymbols.add(String.valueOf(elementString.charAt(i)));
        }
     }
    
    public AtomAugmentor(List<String> elementSymbols) {
        this.elementSymbols = elementSymbols;
     }

    
    @Override
    public List<AtomAugmentation> augment(AtomAugmentation parent) {
        IAtomContainer atomContainer = parent.getAugmentedObject();
        List<AtomAugmentation> augmentations = new ArrayList<AtomAugmentation>();
        ElementConstraints constraints = parent.getConstraints();
        if (constraints == null)
            throw new UnsupportedOperationException("Constraints are null - should not be");
        IChemObjectBuilder builder = getBuilder();
        for (String elementSymbol : constraints) {
            for (int[] bondOrders : getBondOrderArrays(atomContainer, elementSymbol)) {
                IAtom atomToAdd = builder.newInstance(IAtom.class, elementSymbol);
                augmentations.add(
                        new AtomAugmentation(atomContainer, atomToAdd, bondOrders, constraints.minus(elementSymbol)));
            }
        }
        
        return augmentations;
    }
    
    private List<int[]> getBondOrderArrays(IAtomContainer atomContainer, String symbol) {
        AtomContainerDiscretePartitionRefiner refiner = PartitionRefinement.forAtoms().create();
        PermutationGroup autG = refiner.getAutomorphismGroup(atomContainer);
        int atomCount = atomContainer.getAtomCount();

        // these are the atom indices that can have bonds added
        int[] saturationCapacity = saturationCalculator.getSaturationCapacity(atomContainer);
        List<Integer> baseSet = saturationCalculator.getUndersaturatedAtoms(atomCount, saturationCapacity);
        
        int maxDegreeSumForCurrent = saturationCalculator.getMaxBondOrderSum(symbol);
        int maxDegreeForCurrent = saturationCalculator.getMaxBondOrder(symbol);
        
        List<int[]> representatives = new ArrayList<int[]>();
        for (int[] bondOrderArray : saturationCalculator.getBondOrderArrays(
                baseSet, atomCount, maxDegreeSumForCurrent, maxDegreeForCurrent, saturationCapacity)) {
            if (isMinimal(bondOrderArray, autG)) {
                representatives.add(bondOrderArray);
            }
        }

        return representatives;
    }
    
    private boolean isMinimal(int[] bondOrderArray, PermutationGroup autG) {
        String oStr = Arrays.toString(bondOrderArray);
        for (Permutation p : autG.all()) {
//            System.out.println("comparing " + oStr + " and " + p + " of " + Arrays.toString(bondOrderArray));
            String pStr = Arrays.toString(permute(bondOrderArray, p));
            if (oStr.compareTo(pStr) < 0) {
                return false;
            }
        }
        return true;
    }
    
    private int[] permute(int[] a, Permutation p) {
        int[] pA = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            pA[p.get(i)] = a[i];
        }
        return pA;
    }
    
}
