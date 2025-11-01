package client;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.*;
import com.formdev.flatlaf.FlatLightLaf;

import client.ui.ClientUI;
import client.ui.Landing;
import shared.LoadingPopup;

public class ClientApp {

    private Landing landing;
    private ClientUI clientUI;
    private LoadingPopup loadingPopup;

    public ClientApp() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        showLanding();
    }

    /** ------------------------
     *  Landing
     * ------------------------ */
    private void showLanding() {
        landing = new Landing();
        landing.setOnLoginOrRegisterSuccess((String serverIP, String serverPort, String accountName) -> {
            landing.undisplay();
            showLoadingAndOpenClientUI(serverIP, serverPort, accountName);
        });
        landing.display();
    }

    /** ------------------------
     *  ClientUI
     * ------------------------ */
    private void showLoadingAndOpenClientUI(String serverIP, String serverPort, String accountName) {
        loadingPopup = new LoadingPopup(landing.getFrame(), "Loading mail client");
        loadingPopup.display();

        // run heavy task in background
        new Thread(() -> {
            clientUI = new ClientUI(serverIP, serverPort, accountName);
            clientUI.setOnLogout(() -> {
                cleanupLocalStorage(accountName);
                clientUI.undisplay();
                showLanding(); // return to landing on logout
            });
            
            clientUI.setOnExit(() -> {
                cleanupLocalStorage(accountName);
                clientUI.undisplay();
                SwingUtilities.invokeLater(() -> System.exit(0));
            });

            SwingUtilities.invokeLater(() -> {
                loadingPopup.close();
                clientUI.display();
            });
        }).start();
    }
    
    
	/** ------------------------
	 *  Utility: Clean up local storage for user
	 * ------------------------ */
	private void cleanupLocalStorage(String accountName) {
		Path localStoragePath = Paths.get("src", "client", "localStorage", accountName);

		try {
			if (Files.exists(localStoragePath)) {
				deleteDirectoryRecursively(localStoragePath);
//				System.out.println("Deleted local storage for user: " + accountName);
			} else {
				System.out.println("No local storage found for user: " + accountName);
			}
		} catch (IOException e) {
			System.err.println("Failed to delete local storage for " + accountName);
			e.printStackTrace();
		}
	}

	private void deleteDirectoryRecursively(Path path) throws IOException {
		if (Files.notExists(path))
			return;

		// Walk the file tree and delete in reverse order
		Files.walk(path).sorted((a, b) -> b.compareTo(a)) // delete children before parents
			.forEach(p -> {
				try {
					Files.deleteIfExists(p);
				} catch (IOException e) {
					System.err.println("Failed to delete: " + p);
				}
			});
	}
}
