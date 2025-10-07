package server;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.nio.file.Path;

import com.formdev.flatlaf.FlatLightLaf;

import server.ui.ServerUI;

public class Server {
	public static void main(String[] args) {
        // start UDP server in background
        try {
            Path db = Path.of("src", "server", "database");
            UdpMailServer mailServer = new UdpMailServer(5000, db);
            Thread serverThread = new Thread(mailServer);
            serverThread.setDaemon(true);
            serverThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(new FlatLightLaf());
			} catch (UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
			
			ServerUI serverUI = new ServerUI();
			serverUI.display();
		});
	}
}
