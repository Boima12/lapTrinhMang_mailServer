package server;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import com.formdev.flatlaf.FlatLightLaf;

import server.ui.ServerUI;
import server.network.SmtpServer;
import shared.NetworkUtils;

public class Server {

    public static void main(String[] args) {
        String localIP = NetworkUtils.getLocalIPAddress();
        final int smtpPort = 2525;

        System.out.println("Server starting...");
        System.out.println("SMTP Server will run at: " + localIP + ":" + smtpPort + "\n");

        // --- 1. Create UI placeholder (we need it visible to both threads)
        final ServerUI[] uiHolder = new ServerUI[1];

        // --- 2. Launch Swing UI on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }

            ServerUI serverUI = new ServerUI(localIP, smtpPort);
            serverUI.display();

            // keep a reference accessible from main thread
            uiHolder[0] = serverUI;
        });

        // --- 3. Start SMTP server in background
        new Thread(() -> {
            // wait until UI is initialized
            while (uiHolder[0] == null) {
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }

            SmtpServer smtpServer = new SmtpServer(
                localIP,
                smtpPort,
                System.out::println, // console log
                message -> SwingUtilities.invokeLater(() -> {
                    String timestamp = new java.text.SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new java.util.Date());
                    uiHolder[0].addRecordListItem(timestamp, message);
                })
            );

            smtpServer.start();
        }, "SMTP-Server-Init").start();
    }
}
