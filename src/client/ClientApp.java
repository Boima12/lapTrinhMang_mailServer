package client;

import javax.swing.*;
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
        landing.setOnLoginOrRegisterSuccess(() -> {
            landing.undisplay();
            showLoadingAndOpenClientUI();
        });
        landing.display();
    }

    /** ------------------------
     *  ClientUI
     * ------------------------ */
    private void showLoadingAndOpenClientUI() {
        loadingPopup = new LoadingPopup(landing.getFrame(), "Loading mail client");
        loadingPopup.display();

        // run heavy task in background
        new Thread(() -> {
            clientUI = new ClientUI();
            clientUI.setOnLogout(() -> {
                clientUI.undisplay();
                showLanding(); // return to landing on logout
            });

            SwingUtilities.invokeLater(() -> {
                loadingPopup.close();
                clientUI.display();
            });
        }).start();
    }
}
