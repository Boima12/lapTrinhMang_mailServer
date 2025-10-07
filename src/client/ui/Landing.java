package client.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Font;

import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.JButton;

public class Landing {

	private Runnable onLoginOrRegisterSuccess;
    public interface AuthHandler { boolean handle(String username, String password); }
    private AuthHandler loginHandler;
    private AuthHandler registerHandler;
	
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
    	// Hiển thị cửa sổ Landing
    	frame.setVisible(true); 
    }
    
    public void undisplay() {
    	// Ẩn cửa sổ Landing (chuyển sang màn hình khác)
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
		
	public void setOnLoginOrRegisterSuccess(Runnable callback) {
	    this.onLoginOrRegisterSuccess = callback;
	}
	
	public void showLoginDialog() {
	    String[] credentials = LoginDialog.askForLogin(frame);
	    if (credentials != null) {
            String username = ensureEmail(credentials[0]);
	        String password = credentials[1];
            boolean valid = loginHandler != null && loginHandler.handle(username, password);
	        if (valid && onLoginOrRegisterSuccess != null) {
	            onLoginOrRegisterSuccess.run();
	        }
	    }
	}

	public void showRegisterDialog() {
	    String[] credentials = RegisterDialog.askForRegister(frame);
	    if (credentials != null) {
            String username = ensureEmail(credentials[0]);
	        String password = credentials[1];
            boolean accountCreated = registerHandler != null && registerHandler.handle(username, password);
	        if (accountCreated && onLoginOrRegisterSuccess != null) {
	        	onLoginOrRegisterSuccess.run();
	        }
        }
	}
	
	public JFrame getFrame() {
	    return frame;
	}

    public void setLoginHandler(AuthHandler loginHandler) { this.loginHandler = loginHandler; }
    public void setRegisterHandler(AuthHandler registerHandler) { this.registerHandler = registerHandler; }

    private String ensureEmail(String input) {
        String s = input == null ? "" : input.trim();
        if (s.isEmpty()) return s;
        return s.contains("@") ? s : s + "@gmail.com";
    }
}

