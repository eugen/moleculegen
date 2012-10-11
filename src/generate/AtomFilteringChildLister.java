package generate;

import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.signature.MoleculeSignature;

/**
 * List candidate children of a parent molecule, by connecting a new atom to the existing atoms 
 * through a set of bonds that are minimal in their orbit.
 * 
 * @author maclean
 *
 */
public class AtomFilteringChildLister extends BaseAtomChildLister implements ChildLister {
    
    public AtomFilteringChildLister() {
        super();
    }
    
    /**
     * Convenience constructor for testing.
     * 
     * @param elementString
     */
    public AtomFilteringChildLister(String elementString) {
        this();
        setElementString(elementString);
    }
    
    public List<IAtomContainer> listChildren(IAtomContainer parent, int currentAtomIndex) {
        int maxDegreeSumForCurrent = getMaxBondOrderSum(currentAtomIndex);
        int maxDegreeForCurrent = getMaxBondOrder(currentAtomIndex);
        List<IAtomContainer> children = new ArrayList<IAtomContainer>();
        List<String> certs = new ArrayList<String>();
        int maxMultisetSize = Math.min(currentAtomIndex, maxDegreeSumForCurrent);
        
//        System.out.println("listing " + test.AtomContainerPrinter.toString(parent));
        for (int[] bondOrderArray : getBondOrderArrays(parent, maxMultisetSize, maxDegreeForCurrent)) {
            IAtomContainer child = makeChild(parent, bondOrderArray, currentAtomIndex);
            MoleculeSignature molSig = new MoleculeSignature(child);
            String molSigString = molSig.toCanonicalString();
            if (certs.contains(molSigString)) {
//                System.out.println("seen " + molSigString);
                continue;
            } else {
//                System.out.println("new " + molSigString);
                children.add(child);
                certs.add(molSigString);
            }
        }
        return children;
    }
}
