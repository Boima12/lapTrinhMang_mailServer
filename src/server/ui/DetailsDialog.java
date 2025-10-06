package server.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A modal dialog that shows detailed information about a mail record.
 */
public class DetailsDialog {

    /**
     * Shows the details dialog for a mail record.
     * 
     * NOTE: 5 STRING PARAMATERS IS APPROACH IS ONLY TEMPORARY, implement a only string as a address to the .txt file inside localStorage folder
     *
     * @param parent     the parent component for positioning
     * @param title      the mail title
     * @param from       the sender
     * @param to         the receiver
     * @param timestamp  the time the mail was sent
     * @param size       the size of the mail
     */
    public static void showDetails(Component parent,
                                   String title,
                                   String from,
                                   String to,
                                   String timestamp,
                                   String size) {

        // Create the dialog
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Mail Details", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(700, 350);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBounds(10, 10, 660, 280);
        dialog.add(mainPanel);

        JLabel mailTitle = new JLabel("Title: " + title);
        mailTitle.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
        mailTitle.setBounds(10, 10, 640, 25);
        mainPanel.add(mailTitle);

        JLabel mailFrom = new JLabel("FROM: " + from);
        mailFrom.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
        mailFrom.setBounds(10, 45, 640, 25);
        mainPanel.add(mailFrom);

        JLabel mailTo = new JLabel("TO: " + to);
        mailTo.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
        mailTo.setBounds(10, 80, 640, 25);
        mainPanel.add(mailTo);

        JLabel mailTimestamp = new JLabel("Timestamp: " + timestamp);
        mailTimestamp.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
        mailTimestamp.setBounds(10, 115, 640, 25);
        mainPanel.add(mailTimestamp);

        JLabel mailSize = new JLabel("Size: " + size);
        mailSize.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
        mailSize.setBounds(10, 150, 640, 25);
        mainPanel.add(mailSize);

        JButton closeBt = new JButton("Close");
        closeBt.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
        closeBt.setBounds(250, 220, 160, 35);
        closeBt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        mainPanel.add(closeBt);

        dialog.setVisible(true);
    }
}
