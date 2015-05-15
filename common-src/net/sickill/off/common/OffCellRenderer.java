package net.sickill.off.common;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 *
 * @author kill
 */
public class OffCellRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 4562757887321715315L;

    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        OffListElement e = (OffListElement)value;
        ((JLabel)comp).setIcon(e.getIcon());
        ((JLabel)comp).setText(e.getFilename());
        return comp;
    }
}
