package validate;

import group.AtomDiscretePartitionRefiner;
import group.Partition;
import group.Permutation;

import java.util.SortedSet;
import java.util.TreeSet;

import org.openscience.cdk.interfaces.IAtomContainer;

public class RefinementCanonicalValidator implements CanonicalValidator {
    
    private AtomDiscretePartitionRefiner refiner;
    
    public RefinementCanonicalValidator() {
        refiner = new AtomDiscretePartitionRefiner();
    }

    @Override
    public boolean isCanonical(IAtomContainer atomContainer) {
        refiner.reset();
        refiner.getAutomorphismGroup(atomContainer);
        int size = refiner.getVertexCount() - 1;
        
        Permutation labelling = refiner.getBest();
        Permutation inverse = labelling.invert();
        Partition partition = refiner.getAutomorphismPartition();
        partition = translate(partition, inverse);
        int del = inverse.get(size);
        
        boolean canonical = (del == size || inSameCell(partition, del, size));
//        String acp = test.AtomContainerPrinter.toString(atomContainer);
        if (canonical) {
//            System.out.println("CC " + inverse + "\t" + size + "\t" + del + "\t" + partition + "\t" + acp);
            return true;
        } else {
//            System.out.println("CN " + inverse + "\t" + size + "\t" + del + "\t" + partition + "\t" + acp);
            return false;
        }
    }
    
    private Partition translate(Partition original, Permutation labelling) {
        Partition translation = new Partition();
        for (int cellIndex = 0; cellIndex < original.size(); cellIndex++) {
            SortedSet<Integer> oCell = original.getCell(cellIndex);
            SortedSet<Integer> tCell = new TreeSet<Integer>();
            for (int i : oCell) {
                tCell.add(labelling.get(i));
            }
            translation.addCell(tCell);
        }
        return translation;
    }

    private boolean inSameCell(Partition partition, int i, int j) {
        for (int cellIndex = 0; cellIndex < partition.size(); cellIndex++) {
            SortedSet<Integer> cell = partition.getCell(cellIndex);
            if (cell.contains(i) && cell.contains(j)) {
                return true;
            }
        }
        return false;
    }

}
