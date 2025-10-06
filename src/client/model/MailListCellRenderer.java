package client.model;

import javax.swing.*;
import java.awt.*;

public class MailListCellRenderer extends JPanel implements ListCellRenderer<MailListItem> {

    private static final long serialVersionUID = 1L;
    private JLabel titleLabel;
    private JLabel bottomLabel;

    public MailListCellRenderer() {
        setLayout(null); // use absolute layout for pixel-perfect control
        setPreferredSize(new Dimension(270, 50));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEtchedBorder());

        titleLabel = new JLabel();
        titleLabel.setFont(new Font("Sans Serif Collection", Font.PLAIN, 12));
        titleLabel.setBounds(5, 5, 255, 25);

        bottomLabel = new JLabel();
        bottomLabel.setFont(new Font("Sans Serif Collection", Font.PLAIN, 10));
        bottomLabel.setForeground(new Color(88, 88, 88));
        bottomLabel.setBounds(5, 32, 255, 15);

        add(titleLabel);
        add(bottomLabel);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends MailListItem> list,
                                                  MailListItem value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {

        titleLabel.setText(value.getTitle());
        bottomLabel.setText(value.getFrom() + "   " + value.getTimestamp());

        if (isSelected) {
            setBackground(new Color(230, 230, 255));
        } else {
            setBackground(Color.WHITE);
        }

        return this;
    }
}
