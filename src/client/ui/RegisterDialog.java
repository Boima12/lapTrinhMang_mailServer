package client.ui;

import javax.swing.*;
import java.awt.*;

public class RegisterDialog {

    /**
     * Shows a registration dialog and returns username/password if confirmed.
     * @param parent the parent component for dialog positioning
     * @return String[]{username, password} or null if cancelled
     */
    public static String[] askForRegister(Component parent) {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(20);

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(20);

        JLabel confirmLabel = new JLabel("Confirm Password:");
        JPasswordField confirmField = new JPasswordField(20);

        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(confirmLabel);
        panel.add(confirmField);

        String[] options = {"Register", "Cancel"};
        int option = JOptionPane.showOptionDialog(
                parent,
                panel,
                "Register",
                JOptionPane.NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (option == 0) { // Register clicked
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String confirm = new String(confirmField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        "All fields are required.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return askForRegister(parent); // Retry
            }

            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(parent,
                        "Passwords do not match.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return askForRegister(parent); // Retry
            }

            return new String[]{username, password};
        } else {
            return null; // Cancelled
        }
    }
}
