package client.ui;

import javax.swing.*;
import java.awt.*;

public class LoginDialog {

    /**
     * Shows a login dialog and returns server + credentials if confirmed.
     * @param parent the parent component for dialog positioning
     * @return String[]{serverIP, serverPort, username, password} or null if cancelled
     */
    public static String[] askForLogin(Component parent) {
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));

        JLabel ipLabel = new JLabel("Server IP:");
        JTextField ipField = new JTextField("192.168.1.9", 20);

        JLabel portLabel = new JLabel("Server Port:");
        JTextField portField = new JTextField("2525", 20);

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(20);

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(20);

        panel.add(ipLabel);
        panel.add(ipField);
        panel.add(portLabel);
        panel.add(portField);
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

        if (option == 0) {
            String serverIP = ipField.getText().trim();
            String serverPort = portField.getText().trim();
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (serverIP.isEmpty() || serverPort.isEmpty() ||
                username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        "All fields are required.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return askForLogin(parent);
            }

            try {
                Integer.parseInt(serverPort);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parent,
                        "Port must be numeric.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return askForLogin(parent);
            }

            return new String[]{serverIP, serverPort, username, password};
        } else {
            return null;
        }
    }
}
