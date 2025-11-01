package client.ui;

import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import client.network.SmtpClient;
import shared.NetworkUtils;
import shared.mailBuilderUtils;

import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

public class ComposeUI {

	private final mailBuilderUtils mailUtils = new mailBuilderUtils();
	private String accountName;
	private String serverIP;
	private String serverPort;
	
	private JFrame frame;
	private JTextField titleTf;
	private JTextField toAddressTf;
	private JTextArea textArea;
	private JButton btnSend;
	private JButton discardBt;

	public ComposeUI(String serverIP, String serverPort, String accountName) {
		this.accountName = accountName;
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		
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
		frame.setBounds(100, 100, 1200, 700);
//		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setTitle("Write a mail");
		frame.getContentPane().setLayout(null);
		
		// Handle window close (X button)
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent e) {
		        handleDispose();
		    }
		});
		
		JLabel title = new JLabel("Title");
		title.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		title.setBounds(10, 10, 119, 24);
		frame.getContentPane().add(title);
		
		titleTf = new JTextField();
		titleTf.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		titleTf.setColumns(10);
		titleTf.setBounds(10, 35, 900, 30);
		frame.getContentPane().add(titleTf);
		
		JLabel toAddress = new JLabel("To");
		toAddress.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		toAddress.setBounds(10, 75, 203, 24);
		frame.getContentPane().add(toAddress);
		
		toAddressTf = new JTextField();
		toAddressTf.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		toAddressTf.setColumns(10);
		toAddressTf.setBounds(10, 100, 900, 30);
		frame.getContentPane().add(toAddressTf);
		
		// make this textArea can be scrollable vertically
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBounds(10, 140, 1166, 513);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		frame.getContentPane().add(scrollPane);
		
		btnSend = new JButton("Send");
		btnSend.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		btnSend.setBounds(1056, 95, 120, 35);
		btnSend.addActionListener(e -> {
			handleSend(accountName);
		});
		frame.getContentPane().add(btnSend);
		
		discardBt = new JButton("");
		discardBt.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		discardBt.setBounds(1006, 95, 40, 35);
		discardBt.setIcon(new ImageIcon(new ImageIcon(ComposeUI.class.getResource("../assets/discardMessage.png")).getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
		frame.getContentPane().add(discardBt);
	}
	
	public void handleDispose() {
	    int confirm = JOptionPane.showConfirmDialog(
	        frame,
	        "Are you sure you want to abort this mail?",
	        "Confirm abort",
	        JOptionPane.YES_NO_OPTION
	    );

	    if (confirm == JOptionPane.YES_OPTION) {   	
	        frame.dispose(); 
	    }
	}
	
	public void handleSend(String accountName) {
	    try {
	        int confirm = JOptionPane.showConfirmDialog(
	            frame,
	            "Send this mail?",
	            "Confirm sending",
	            JOptionPane.YES_NO_OPTION
	        );

	        if (confirm == JOptionPane.YES_OPTION) {
	            frame.dispose();

	            // --- 1. Prepare mail metadata ---
	            String clientIP = NetworkUtils.getLocalIPAddress();
	            String title = titleTf.getText().trim();
	            String toAddress = toAddressTf.getText().trim();
	            String content = textArea.getText().trim();

	            // --- 2. Validate input fields ---
	            if (title.isEmpty()) {
	                JOptionPane.showMessageDialog(frame, "Please enter a title for your mail.", "Missing Title", JOptionPane.WARNING_MESSAGE);
	                return;
	            }

	            if (toAddress.isEmpty() || !toAddress.endsWith("@mailServer.com")) {
	                JOptionPane.showMessageDialog(frame, "Please provide a valid recipient address (e.g., user@mailServer.com).", "Invalid Address", JOptionPane.WARNING_MESSAGE);
	                return;
	            }

	            if (content.isEmpty()) {
	                int confirmEmpty = JOptionPane.showConfirmDialog(
	                    frame,
	                    "Mail body is empty. Do you still want to send it?",
	                    "Empty Mail Body",
	                    JOptionPane.YES_NO_OPTION
	                );
	                if (confirmEmpty != JOptionPane.YES_OPTION) return;
	            }

	            // --- 3. Create mail locally in sent/ ---
	            File sentDir = new File("src/client/localStorage/" + accountName + "/sent");
	            File newMail = mailUtils.createEmail(
	                sentDir,
	                title,
	                accountName,
	                clientIP,
	                toAddress,
	                content
	            );

	            // --- 4. Send mail to the server via SMTP ---
	            new Thread(() -> {
	                try (SmtpClient smtp = new SmtpClient(serverIP, Integer.parseInt(serverPort))) {
	                    smtp.sendEmail(newMail, accountName);
	                    SwingUtilities.invokeLater(() ->
	                        JOptionPane.showMessageDialog(frame, "Mail sent successfully!", "Success", JOptionPane.INFORMATION_MESSAGE)
	                    );
	                } catch (IOException ex) {
	                    SwingUtilities.invokeLater(() ->
	                        JOptionPane.showMessageDialog(frame, "Failed to send mail.", "Error", JOptionPane.ERROR_MESSAGE)
	                    );
	                }
	            }).start();


	            // --- 5. Clear UI fields for safety ---
	            titleTf.setText("");
	            toAddressTf.setText("");
	            textArea.setText("");
	        }
	        
	    } catch (IOException e) {
	        e.printStackTrace();
	        JOptionPane.showMessageDialog(frame, "Unexpected error while sending mail.", "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}

}
