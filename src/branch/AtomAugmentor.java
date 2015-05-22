package branch;

import group.AtomDiscretePartitionRefiner;
import group.Permutation;
import group.PermutationGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

public class AtomAugmentor implements Augmentor<IAtomContainer> {
    
    /**
     * The elements (in order) used to make molecules for this run.
     */
    private List<String> elementSymbols;
    
    private IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
    
    private SaturationCalculator saturationCalculator;
    
    public AtomAugmentor(String elementString) {
        elementSymbols = new ArrayList<String>();
        for (int i = 0; i < elementString.length(); i++) {
            elementSymbols.add(String.valueOf(elementString.charAt(i)));
        }
        this.saturationCalculator = new SaturationCalculator(elementSymbols);
    }
    
    /**
     * @return the initial structure
     */
    public Augmentation<IAtomContainer> getInitial() {
        String elementSymbol = elementSymbols.get(0);
        IAtom initialAtom = builder.newInstance(IAtom.class, elementSymbol);
        return new AtomAugmentation(initialAtom);
    }

    @Override
    public List<Augmentation<IAtomContainer>> augment(Augmentation<IAtomContainer> parent) {
        IAtomContainer atomContainer = parent.getAugmentedMolecule();
        List<Augmentation<IAtomContainer>> augmentations = new ArrayList<Augmentation<IAtomContainer>>();
        String elementSymbol = getNextElementSymbol(parent);
        for (int[] bondOrders : getBondOrderArrays(atomContainer)) {
            IAtom atomToAdd = builder.newInstance(IAtom.class, elementSymbol);
            augmentations.add(new AtomAugmentation(atomContainer, atomToAdd, bondOrders));
        }
        
        return augmentations;
    }
    
    private String getNextElementSymbol(Augmentation<IAtomContainer> parent) {
        int index = parent.getAugmentedMolecule().getAtomCount() - 1;
        if (index < elementSymbols.size()) {
            return elementSymbols.get(index);
        } else {
            return "C"; // XXX TODO...
        }
    }
    
    private List<int[]> getBondOrderArrays(IAtomContainer atomContainer) {
        AtomDiscretePartitionRefiner refiner = new AtomDiscretePartitionRefiner();
        PermutationGroup autG = refiner.getAutomorphismGroup(atomContainer);
        int atomCount = atomContainer.getAtomCount();
        
        // these are the atom indices that can have bonds added
        int[] saturationCapacity = saturationCalculator.getSaturationCapacity(atomContainer);
        List<Integer> baseSet = getUndersaturatedSet(atomCount, saturationCapacity);
        
        int maxDegreeSumForCurrent = saturationCalculator.getMaxBondOrderSum(atomCount - 1);
        int maxDegreeForCurrent = saturationCalculator.getMaxBondOrder(atomCount - 1);
        
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
    
    private List<Integer> getUndersaturatedSet(int atomCount, int[] saturationCapacity) {
        List<Integer> baseSet = new ArrayList<Integer>();
        
        // get the amount each atom is under-saturated
        for (int index = 0; index < atomCount; index++) {
            if (saturationCapacity[index] > 0) {
                baseSet.add(index);
            }
        }
        return baseSet;
    }
    
   

}
