import javax.swing.table.DefaultTableModel;

/**
 * Created by ����� on 06/05/2019.
 */
public class RFIDTableModel extends DefaultTableModel {

    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
