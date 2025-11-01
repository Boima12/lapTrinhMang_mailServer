package client.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import java.io.IOException;

import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;

import client.network.SmtpClient;
import client.ui.listeners.LoginSuccessListener;

import javax.swing.JButton;

public class Landing {

	private LoginSuccessListener onLoginOrRegisterSuccess;
	
	private JFrame frame;
	private JButton loginBt;
	private JButton registerBt;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(new FlatLightLaf());
					
					Landing window = new Landing();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Landing() {
		initialize();
	}

    public void display() { 
    	frame.setVisible(true); 
    }
    
    public void undisplay() {
    	frame.setVisible(false);
    }

	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Landing panel (Client)");
		
		JPanel landingPanel = new JPanel();
		landingPanel.setBounds(10, 10, 416, 443);
		frame.getContentPane().add(landingPanel);
		landingPanel.setLayout(null);
		
		JLabel mailServerLabel = new JLabel("Mail Server");
		mailServerLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mailServerLabel.setFont(new Font("Sans Serif Collection", Font.PLAIN, 20));
		mailServerLabel.setBounds(135, 30, 133, 75);
		landingPanel.add(mailServerLabel);
		
		loginBt = new JButton("Login");
		loginBt.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		loginBt.setBounds(100, 161, 200, 40);
		loginBt.addActionListener(e -> {
            showLoginDialog();
        });
		landingPanel.add(loginBt);
		
		registerBt = new JButton("Register");
		registerBt.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		registerBt.setBounds(100, 228, 200, 40);
		registerBt.addActionListener(e -> {
            showRegisterDialog();
        });
		landingPanel.add(registerBt);
	}
		
	public void setOnLoginOrRegisterSuccess(LoginSuccessListener callback) {
	    this.onLoginOrRegisterSuccess = callback;
	}
	
	public void showLoginDialog() {
	    String[] credentials = LoginDialog.askForLogin(frame);
	    if (credentials != null) {
	        String serverIP = credentials[0];
	        String serverPort = credentials[1];
	        String username = credentials[2];
	        String password = credentials[3];
	        System.out.println("Login as: " + username + " / " + password);

	        try (SmtpClient smtp = new SmtpClient(serverIP, Integer.parseInt(serverPort))) {
	            boolean valid = smtp.login(username, password);

	            if (valid) {
	                JOptionPane.showMessageDialog(frame, "Login successful!");
	                if (onLoginOrRegisterSuccess != null) {
	                    onLoginOrRegisterSuccess.onSuccess(serverIP, serverPort, username);
	                }
	            } else {
	                JOptionPane.showMessageDialog(frame, "Invalid username or password.",
	                        "Login Failed", JOptionPane.ERROR_MESSAGE);
	            }
	        } catch (IOException e) {
	            JOptionPane.showMessageDialog(frame, "Cannot reach server",
	                    "Network Error", JOptionPane.ERROR_MESSAGE);
	            e.printStackTrace();
	        }
	    }
	}

	public void showRegisterDialog() {
	    String[] credentials = RegisterDialog.askForRegister(frame);
	    if (credentials != null) {
	    	String serverIP = credentials[0];
	    	String serverPort = credentials[1]; 
	        String username = credentials[2];
	        String password = credentials[3];
	        System.out.println("Register new user: " + username + " / " + password);

	        try (SmtpClient smtp = new SmtpClient(serverIP, Integer.parseInt(serverPort))) {
	            boolean accountCreated = smtp.register(username, password);

	            if (accountCreated) {
	                JOptionPane.showMessageDialog(frame, "Account created successfully!");
	                if (onLoginOrRegisterSuccess != null) {
	                    onLoginOrRegisterSuccess.onSuccess(serverIP, serverPort, username);
	                }
	            } else {
	                JOptionPane.showMessageDialog(frame, "Registration failed", "Error", JOptionPane.ERROR_MESSAGE);
	            }

	        } catch (IOException e) {
	            JOptionPane.showMessageDialog(frame, "Cannot reach server", "Network Error", JOptionPane.ERROR_MESSAGE);
	            e.printStackTrace();
	        }
	    }
	}

	
	public JFrame getFrame() {
	    return frame;
	}
}

