package generate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IBond.Order;
import org.openscience.cdk.signature.MoleculeSignature;

import combinatorics.MultiKSubsetLister;

public class BaseAtomChildLister {
    
    /**
     * TODO : this is a very crude method
     * The max BOS is the maximum sum of bond orders of the bonds 
     * attached to an atom of this element type. eg if maxBOS = 4, 
     * then the atom can have any of {{4}, {3, 1}, {2, 2}, {2, 1, 1}, ...} 
     */
    private Map<String, Integer> maxBondOrderSumMap;
    
    /**
     * TODO : this is a very crude method
     * The max bond order is the maximum order of any bond attached.
     */
    private Map<String, Integer> maxBondOrderMap;
    
    /**
     * The elements (in order) used to make this molecule.
     */
    private List<String> elementSymbols;
    
    public BaseAtomChildLister() {
        maxBondOrderSumMap = new HashMap<String, Integer>();
        maxBondOrderSumMap.put("C", 4);
        maxBondOrderSumMap.put("O", 2);
        maxBondOrderSumMap.put("N", 5);
        maxBondOrderSumMap.put("S", 6);
        maxBondOrderSumMap.put("P", 5);
        maxBondOrderSumMap.put("F", 1);
        maxBondOrderSumMap.put("I", 1);
        maxBondOrderSumMap.put("Cl", 1);
        
        maxBondOrderMap = new HashMap<String, Integer>();
        maxBondOrderMap.put("C", 3);
        maxBondOrderMap.put("O", 3);
        maxBondOrderMap.put("N", 3);
        maxBondOrderMap.put("S", 2);
        maxBondOrderMap.put("P", 2);
        maxBondOrderMap.put("F", 1);
        maxBondOrderMap.put("I", 1);
        maxBondOrderMap.put("Cl", 1);
    }
    
    public BaseAtomChildLister(String elementString) {
        this();
        elementSymbols = new ArrayList<String>();
        for (int i = 0; i < elementString.length(); i++) {
            elementSymbols.add(String.valueOf(elementString.charAt(i)));
        }
    }
    
    public List<String> getElementSymbols() {
        return elementSymbols;
    }
    
    public String getCertificate(IAtomContainer atomContainer) {
        return new MoleculeSignature(atomContainer).toCanonicalString();
    }
    
    public int getMaxBondOrderSum(int index) {
        return maxBondOrderSumMap.get(elementSymbols.get(index));
    }

    public int getMaxBondOrder(int currentAtomIndex) {
        return maxBondOrderMap.get(elementSymbols.get(currentAtomIndex));
    }
    
    public void setElementSymbols(List<String> elementSymbols) {
        this.elementSymbols = elementSymbols;
    }
    
    public IAtomContainer makeChild(
            IAtomContainer parent, int[] bondOrderArr, int lastIndex) {
        try {
            IAtomContainer child = (IAtomContainer) parent.clone();
            String atomSymbol = elementSymbols.get(lastIndex);
            child.addAtom(child.getBuilder().newInstance(IAtom.class, atomSymbol));
            for (int index = 0; index < bondOrderArr.length; index++) {
                int value = bondOrderArr[index];
                if (value > 0) {
                    Order order;
                    switch (value) {
                        case 1: order = Order.SINGLE; break;
                        case 2: order = Order.DOUBLE; break;
                        case 3: order = Order.TRIPLE; break;
                        default: order = Order.SINGLE;
                    }
                    child.addBond(index, lastIndex, order);
                }
            }
//            System.out.println(java.util.Arrays.toString(bondOrderArr) + "\t" 
//                    + test.AtomContainerPrinter.toString(child));
            return child;
        } catch (CloneNotSupportedException cnse) {
            // TODO
            return null;
        }
    }
    
    public List<int[]> getBondOrderArrays(
            IAtomContainer parent, int currentAtomIndex, int maxDegreeSumForCurrent, int maxDegree) {
        // these are the atom indices that can have bonds added
        List<Integer> baseSet = new ArrayList<Integer>();
        
        // get the amount each atom is under-saturated
        int[] saturationCapacity = getSaturationCapacity(parent);
        for (int index = 0; index < parent.getAtomCount(); index++) {
            if (saturationCapacity[index] > 0) {
                baseSet.add(index);
            }
        }
        
        // the possible extensions
        List<int[]> bondOrderArrays = new ArrayList<int[]>();
        
        // no extension possible
        if (baseSet.size() == 0) {
            return bondOrderArrays;
        }
        
        for (int k = 1; k <= maxDegreeSumForCurrent; k++) {
            MultiKSubsetLister<Integer> lister = new MultiKSubsetLister<Integer>(k, baseSet);
            for (List<Integer> multiset : lister) {
                int[] bondOrderArray = 
                    toIntArray(multiset, parent.getAtomCount(), maxDegree, saturationCapacity);
                if (bondOrderArray != null) {
//                    System.out.println(
//                            "converting to " + java.util.Arrays.toString(bondOrderArray)
//                            + " from " + multiset 
//                            + " for " + test.AtomContainerPrinter.toString(parent)
//                    );
                    bondOrderArrays.add(bondOrderArray);
                }
            }
        }
        return bondOrderArrays;
    }
    
    public int[] toIntArray(List<Integer> multiset, int size, int maxDegree, int[] satCap) {
        int[] intArray = new int[size];
        for (int atomIndex : multiset) {
            if (atomIndex >= size) return null; // XXX
            intArray[atomIndex]++;
            // XXX avoid quadruple bonds and oversaturation 
            if (intArray[atomIndex] > maxDegree || intArray[atomIndex] > satCap[atomIndex]) {
                return null;   
            }
        }
//        System.out.println(multiset + "\t" + Arrays.toString(intArray) + "\t" + Arrays.toString(satCap));
        return intArray;
    }
    
    private int[] getSaturationCapacity(IAtomContainer parent) {
        int[] satCap = new int[parent.getAtomCount()];
        for (int index = 0; index < parent.getAtomCount(); index++) {
            IAtom atom = parent.getAtom(index);
            int maxDegree = maxBondOrderSumMap.get(atom.getSymbol());
            int degree = 0;
            for (IBond bond : parent.getConnectedBondsList(atom)) {
                degree += bond.getOrder().ordinal() + 1;
            }
            satCap[index] = maxDegree - degree;
        }
        return satCap;
    }
}
