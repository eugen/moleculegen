package augment.atom;

import java.io.Serializable;

/**
 * The atom and bond information to add when augmenting.
 * 
 * @author maclean
 *
 */
public class AtomExtension implements Serializable {
    
    private final String elementSymbol;
    
    private final int[] bondOrderList;

    public AtomExtension(String elementSymbol, int[] bondOrderList) {
        this.elementSymbol = elementSymbol;
        this.bondOrderList = bondOrderList;
    }

    public String getElementSymbol() {
        return elementSymbol;
    }

    public int[] getBondOrderList() {
        return bondOrderList;
    }

}
