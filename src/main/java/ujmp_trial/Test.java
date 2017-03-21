package ujmp_trial;

import java.util.ArrayList;
import java.util.List;
import org.ujmp.core.SparseMatrix;
import org.ujmp.core.calculation.Calculation;

/**
 *
 * @author Patricia Fischer
 */
public class Test {
    public static void main(String[] args) {
        
        SparseMatrix sm = SparseMatrix.Factory.zeros(3,5);
        for (int i = 1; i < sm.getRowCount(); i++)
            sm.setRowLabel(i, i);
        for (int j = 1; j < sm.getColumnCount(); j++)
            sm.setColumnLabel(j, j);
        sm.setAsDouble(2, 0, 1); // value, row, column
        sm.setAsDouble(3, 1, 3);
        sm.setAsDouble(5, 2, 3);
        
        
        System.out.println("Raw matrix\n"+sm);
        
        System.out.println("Cosine matrix\n"+sm.cosineSimilarity(Calculation.Ret.NEW, true));
        
        //System.out.println("Summed matrix\n"+sm.sum(Calculation.Ret.NEW, 0, true));
        
        
        for(long[] l : sm.nonZeroCoordinates()) {
            System.out.println("Coordinates");
            System.out.println(l[0]);
            System.out.println(l[1]);
            System.out.println("");
        }
        
        List<Long> l = new ArrayList();
        l.add((long)1);
        l.add((long)2);
        
        SparseMatrix sm2 = SparseMatrix.Factory.zeros(5, 7);
        
        sm2.setAsDouble(3, 2, 1);
        sm2.setAsDouble(5, 4, 5);
        
        System.out.println(sm2);
        
        sm2 = SparseMatrix.Factory.zeros(0, sm2.getColumnCount());
        
        SparseMatrix sel = (SparseMatrix) sm2.selectRows(Calculation.Ret.NEW, new ArrayList(l));
        
        
        System.out.println(sel.appendVertically(Calculation.Ret.NEW, sm2));
        
    }
}
