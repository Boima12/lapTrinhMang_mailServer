package shared;

import javax.swing.*;
import java.awt.*;

public class LoadingPopup extends JDialog {

	private static final long serialVersionUID = 1L;
	private JLabel loadingLabel;

    /**
     * Creates a loading popup dialog with a custom message.
     * @param parent the parent component (can be null)
     * @param message the message to show, e.g. "Loading mail client"
     */
    public LoadingPopup(Window parent, String message) {
        super(parent, "Loading", ModalityType.APPLICATION_MODAL);
        initialize(message);
    }

    private void initialize(String message) {
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setSize(300, 150);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(getParent());

        // Label with dynamic message
        loadingLabel = new JLabel(message + "...");
        loadingLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Optional: add a small spinner icon for a better look
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(loadingLabel, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(panel, BorderLayout.CENTER);

        // Add indeterminate progress bar at the bottom
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        add(progressBar, BorderLayout.SOUTH);
    }

    /**
     * Shows the loading popup (non-blocking).
     */
    public void display() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

    /**
     * Closes the loading popup.
     */
    public void close() {
        SwingUtilities.invokeLater(() -> dispose());
    }

    /**
     * Updates the message displayed.
     */
    public void setMessage(String message) {
        loadingLabel.setText(message + "...");
    }
}
