package handler.molecule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.signature.MoleculeSignature;

import handler.Handler;

public class DuplicateCountingHandler implements Handler<IAtomContainer> {
    
    private Map<String, IAtomContainer> sigMap;
    
    private Map<String, List<IAtomContainer>> dupMap;
    
    public DuplicateCountingHandler() {
        sigMap = new HashMap<String, IAtomContainer>();
        dupMap = new HashMap<String, List<IAtomContainer>>();
    }

    @Override
    public void handle(IAtomContainer atomContainer) {
        String canonicalSignature = new MoleculeSignature(atomContainer).toCanonicalString();
        if (sigMap.containsKey(canonicalSignature)) {
            if (dupMap.containsKey(canonicalSignature)) {
                dupMap.get(canonicalSignature).add(atomContainer);
            } else {
                addToDupMap(atomContainer, canonicalSignature);
            }
        } else {
            sigMap.put(canonicalSignature, atomContainer);
            addToDupMap(atomContainer, canonicalSignature);
        }
    }
    
    private void addToDupMap(IAtomContainer atomContainer, String canonicalSignature) {
        IAtomContainer original = sigMap.get(canonicalSignature);
        List<IAtomContainer> dups = new ArrayList<IAtomContainer>();
        dups.add(original);
        dupMap.put(canonicalSignature, dups);
    }
    
    public Map<String, List<IAtomContainer>> getDupMap() {
        return dupMap;
    }

    @Override
    public void finish() {
        
    }

}
