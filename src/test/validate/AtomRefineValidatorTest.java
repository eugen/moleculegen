package test.validate;

import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

import test.AtomContainerPrinter;
import validate.RefinementCanonicalValidator;

public class AtomRefineValidatorTest {
    
    private static final IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
    
    public void testPair(String acpA, String acpB, boolean isConnected) {
        RefinementCanonicalValidator validator = new RefinementCanonicalValidator();
        
        IAtomContainer acA = AtomContainerPrinter.fromString(acpA, builder);
        acA.setProperty("IS_CONNECTED", isConnected);
        boolean canonA = validator.isCanonical(acA);
        
        IAtomContainer acB = AtomContainerPrinter.fromString(acpB, builder);
        acB.setProperty("IS_CONNECTED", isConnected);
        boolean canonB = validator.isCanonical(acB);
        
        System.out.println("A " + canonA + " B " + canonB);
    }
    
    public void testSingle(String acp, boolean isConnected) {
        RefinementCanonicalValidator validator = new RefinementCanonicalValidator();
        IAtomContainer ac = AtomContainerPrinter.fromString(acp, builder);
        ac.setProperty("IS_CONNECTED", isConnected);
        boolean canon = validator.isCanonical(ac);
        System.out.println(canon);
    }
    
    @Test
    public void C3O3_1Test() {
        String acpA = "C0C1C2O3O4O5 0:1(1),0:3(1),2:3(1),0:4(1),0:5(1),4:5(1)";
        String acpB = "C0C1C2O3O4O5 0:2(1),0:3(1),1:3(1),0:4(1),0:5(1),4:5(1)";
        testPair(acpA, acpB, true);
    }
    
    @Test
    public void C3O3_2Test() {
        String acpA = "C0C1C2O3 0:1(1),0:3(1),2:3(1)";
        String acpB = "C0C1C2O3 0:2(1),0:3(1),1:3(1)";
        testPair(acpA, acpB, true);
    }
    
    @Test
    public void C3O3_3Test() {
        String acpA = "C0C1C2 0:1(1)";
        String acpB = "C0C1C2 0:2(1)";
        testPair(acpA, acpB, false);
    }
    
    @Test
    public void disconnTest() {
        testSingle("C0C1C2O3O4O5 0:1(1),0:2(1),1:3(2),0:5(1),2:5(1)", false);
    }

}
