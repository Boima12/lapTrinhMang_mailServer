package server.model;

import javax.swing.*;
import java.awt.*;

public class RecordListCellRenderer extends JPanel implements ListCellRenderer<RecordListItem> {
    
    private static final long serialVersionUID = 1L;
    private JLabel recordLabel;

    public RecordListCellRenderer() {
        setLayout(null);
        setPreferredSize(new Dimension(1156, 22));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230))); // subtle separator

        recordLabel = new JLabel();
        recordLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        recordLabel.setBounds(5, 2, 1156, 20);
        add(recordLabel);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends RecordListItem> list,
                                                  RecordListItem value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {

        recordLabel.setText("[" + value.getTimestamp() + "] " + value.getMessage());

        if (isSelected) {
            setBackground(new Color(230, 230, 255));
        } else {
            setBackground(Color.WHITE);
        }

        return this;
    }
}
