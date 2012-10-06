package generate;

import handler.GenerateHandler;
import handler.PrintStreamHandler;

import java.util.List;

import org.openscience.cdk.interfaces.IAtomContainer;

import validate.MoleculeValidator;
import validate.SimpleValidator;

public class AtomAugmentingGenerator {

    private GenerateHandler handler;

    private MoleculeValidator validator;
    
    private ChildLister childLister;

    public AtomAugmentingGenerator() {
        this(new PrintStreamHandler());
    }

    public AtomAugmentingGenerator(GenerateHandler handler) {
        this.handler = handler;
        validator = new SimpleValidator();
        childLister = new AtomSymmetricChildLister();
    }

    public void extend(IAtomContainer parent, int currentAtomIndex, int size) {
        List<IAtomContainer> children = childLister.listChildren(parent, currentAtomIndex);

        for (IAtomContainer child : children) {
            if (validator.isCanonical(parent, child)) {
                if (validator.isConnected(child) && validator.isValidMol(child)) {
                    handler.handle(parent, child);
                }
                extend(child, currentAtomIndex + 1, size);
            }
        }
    }
}
