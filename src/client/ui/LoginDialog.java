package client.ui;

import javax.swing.*;
import java.awt.*;

public class LoginDialog {

    /**
     * Shows a login dialog and returns username/password if confirmed.
     * @param parent the parent component for dialog positioning
     * @return String[]{username, password} or null if cancelled
     */
    public static String[] askForLogin(Component parent) {
        // Panel layout
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(20);

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(20);

        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);

        String[] options = {"Login", "Cancel"};
        int option = JOptionPane.showOptionDialog(
                parent,
                panel,
                "Login",
                JOptionPane.NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (option == 0) { // Login clicked
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        "Username and password cannot be empty.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return askForLogin(parent); // Retry
            }

            return new String[]{username, password};
        } else {
            return null; // Cancelled
        }
    }
}
