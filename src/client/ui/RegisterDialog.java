package client.ui;

import javax.swing.*;
import java.awt.*;

public class RegisterDialog {

    /**
     * Shows a registration dialog and returns server info + credentials if confirmed.
     * @param parent the parent component for dialog positioning
     * @return String[]{serverIP, serverPort, username, password} or null if cancelled
     */
    public static String[] askForRegister(Component parent) {
        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));

        // --- Server Information ---
        JLabel ipLabel = new JLabel("Server IP:");
        JTextField ipField = new JTextField("192.168.1.9", 20);

        JLabel portLabel = new JLabel("Server Port:");
        JTextField portField = new JTextField("2525", 20);

        // --- User Information ---
        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(20);

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(20);

        JLabel confirmLabel = new JLabel("Confirm Password:");
        JPasswordField confirmField = new JPasswordField(20);

        // --- Add components to panel ---
        panel.add(ipLabel);
        panel.add(ipField);
        panel.add(portLabel);
        panel.add(portField);
        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(confirmLabel);
        panel.add(confirmField);

        // --- Dialog setup ---
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
            String serverIP = ipField.getText().trim();
            String serverPort = portField.getText().trim();
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String confirm = new String(confirmField.getPassword()).trim();

            // --- Validation ---
            if (serverIP.isEmpty() || serverPort.isEmpty() ||
                username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        "All fields are required.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return askForRegister(parent); // Retry
            }

            try {
                Integer.parseInt(serverPort);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parent,
                        "Port must be a valid number.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return askForRegister(parent);
            }

            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(parent,
                        "Passwords do not match.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return askForRegister(parent); // Retry
            }

            // --- Return data in correct order ---
            return new String[]{serverIP, serverPort, username, password};
        } else {
            return null; // Cancelled
        }
    }
}
