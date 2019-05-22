/**
 * Created by Алекс on 06/05/2019.
 */
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

class RFIDRenderer extends DefaultTableCellRenderer {

    int[] status = new int[100];

    public RFIDRenderer() {
        setOpaque(true);
    }

    public void setConstraints(int[] marks){
        for(int i = 0; i<marks.length; i++ ) {
            this.status[i] = marks[i];
        }
    }

    public void initValues(){
        for(int i = 0; i<100; i++ ) {
            this.status[i] = 0;
        }
    }

    //@Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);

        if (this.status[row] == 1){
            setBackground(Color.green);
            setForeground(Color.black);
        } else if (this.status[row] == 2) {
            setBackground(Color.yellow);
            setForeground(Color.black);
        } else if (this.status[row] == 3) {
            setBackground(Color.pink);
            setForeground(Color.black);
        } else {
            setBackground(Color.white);
            setForeground(Color.black);
        }
        setValue(value);
        return this;
    }
}