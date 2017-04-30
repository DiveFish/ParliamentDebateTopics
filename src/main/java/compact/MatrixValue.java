package compact;

import java.io.Serializable;

/**
 * Created by patricia on 19/04/17.
 */
public class MatrixValue implements Serializable {
     int row;
     int col;
    double val;

     public MatrixValue() {
         row = -1;
         col = -1;
         val = -1;
     }

     public MatrixValue(int row, int col, double val) {
         this.row = row;
         this.col = col;
         this.val = val;
     }

    public int getRow() {
        return row;
    }

     public int getColumn() {
         return col;
     }

    public double getValue() {
        return val;
    }
}
