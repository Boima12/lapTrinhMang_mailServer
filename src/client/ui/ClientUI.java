package client.ui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.Image;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import client.model.*;
import client.network.SmtpClient;
import shared.FileNameEncryptor;
import javax.swing.border.LineBorder;

public class ClientUI {

	private Runnable onLogout;
	private Runnable onExit;
	private String accountName;
	private String serverIP;
	private String serverPort;
	
	private JFrame frame;
	private JButton inboxTypeBt;
	private JButton sentTypeBt;
	private JButton composeBt;
	private JLabel header_title;
	private JLabel header_from;
	private JLabel header_timestamp;
	private JTextArea bodyTa;
	private DefaultListModel<MailListItem> inboxListModel;
	private JList<MailListItem> inboxList;
	private JScrollPane inboxListScrollPane;
	private DefaultListModel<MailListItem> sentListModel;
	private JList<MailListItem> sentList;
	private JScrollPane sentListScrollPane;

	/**
	 * Launch the application. (disabled due to ClientUI will request accountName folder data from server, this part is safe to delete)
	 */
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					UIManager.setLookAndFeel(new FlatLightLaf());
//
//					ClientUI window = new ClientUI("DEV ONLY!");
//					window.frame.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}

	public ClientUI(String serverIP, String serverPort, String accountName) {
		this.accountName = accountName;
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		
		initialize();
		requestFolderData(serverIP, serverPort);
		loadInboxList();
		loadSentList();
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
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setTitle("Mail server client - " + accountName);
		frame.getContentPane().setLayout(null);
		
		// Handle window close (X button)
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent e) {
		        handleExit();
		    }
		});
		
        // JmenuBar
        JMenuBar menuBar = new JMenuBar();
        JMenu menuHome = new JMenu("Home");
        menuBar.add(menuHome);
        frame.setJMenuBar(menuBar);
        
        JMenuItem menuRefresh = new JMenuItem("Refresh");
        menuRefresh.addActionListener(e -> {
            refreshData();
        });
        menuHome.add(menuRefresh);
        
        JMenuItem menuLogout = new JMenuItem("Logout");
        menuLogout.addActionListener(e -> {
            handleLogout();
        });
        menuHome.add(menuLogout);
        
        JMenuItem menuExit = new JMenuItem("Exit");
        menuExit.addActionListener(e -> {
            handleExit();
        });
        menuHome.add(menuExit);
		
        // Main content
		JPanel leftPanel = new JPanel();
		leftPanel.setBorder(new SoftBevelBorder(BevelBorder.RAISED, null, null, null, null));
		leftPanel.setBounds(0, 0, 270, 663);
		leftPanel.setLayout(null);
		frame.getContentPane().add(leftPanel);
		
		JPanel mailTypes = new JPanel();
		mailTypes.setBounds(0, 0, 270, 35);
		mailTypes.setLayout(null);
		leftPanel.add(mailTypes);
		
		inboxTypeBt = new JButton("");
		inboxTypeBt.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		inboxTypeBt.setBounds(0, 0, 135, 35);
		inboxTypeBt.setBackground(new Color(220, 220, 255));
		inboxTypeBt.setIcon(new ImageIcon(new ImageIcon(ClientUI.class.getResource("../assets/inbox.png")).getImage().getScaledInstance(17, 17, Image.SCALE_SMOOTH)));
		inboxTypeBt.addActionListener(e -> {
		    inboxListScrollPane.setVisible(true);
		    sentListScrollPane.setVisible(false);
		    inboxTypeBt.setBackground(new Color(220, 220, 255));
		    sentTypeBt.setBackground(Color.WHITE);
        });
		mailTypes.add(inboxTypeBt);
		
		sentTypeBt = new JButton("");
		sentTypeBt.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		sentTypeBt.setBounds(135, 0, 135, 35);
		sentTypeBt.setBackground(Color.WHITE);
		sentTypeBt.setIcon(new ImageIcon(new ImageIcon(ClientUI.class.getResource("../assets/sentmail.png")).getImage().getScaledInstance(17, 17, Image.SCALE_SMOOTH)));
		sentTypeBt.addActionListener(e -> {
			inboxListScrollPane.setVisible(false);
		    sentListScrollPane.setVisible(true);
		    sentTypeBt.setBackground(new Color(220, 220, 255));
		    inboxTypeBt.setBackground(Color.WHITE);
        });
		mailTypes.add(sentTypeBt);
		
		composeBt = new JButton("");
		composeBt.setBackground(new Color(255, 255, 255));
		composeBt.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		composeBt.setBounds(0, 623, 270, 40);
		composeBt.setIcon(new ImageIcon(new ImageIcon(ClientUI.class.getResource("../assets/compose.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
		composeBt.addActionListener(e -> {
            showComposeUi();
        });
		leftPanel.add(composeBt);
		
		inboxListModel = new DefaultListModel<>();
		inboxList = new JList<>(inboxListModel);
		inboxList.setCellRenderer(new MailListCellRenderer());
		inboxList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		inboxListScrollPane = new JScrollPane(inboxList);
		inboxListScrollPane.setBounds(0, 34, 270, 585);
		inboxListScrollPane.setBorder(null); 
		inboxListScrollPane.setVisible(true);
		leftPanel.add(inboxListScrollPane);
		// Add listener for clicks
		inboxList.addListSelectionListener(e -> {
		    if (!e.getValueIsAdjusting()) {
		        MailListItem selected = inboxList.getSelectedValue();
		        if (selected != null) {
		        	String encryptedName = FileNameEncryptor.encryptFileName(selected.getTitle());
		        	File mailFile = new File("src/client/localStorage/" + accountName + "/inbox/" + encryptedName + ".txt");
		            if (mailFile.exists()) {
		                displayMailDetails(mailFile, true);
		            } else {
		                JOptionPane.showMessageDialog(frame, "Mail file not found for: " + selected.getTitle(),
		                        "Missing File", JOptionPane.WARNING_MESSAGE);
		            }
		        }
		    }
		});
		
		sentListModel = new DefaultListModel<>();
		sentList = new JList<>(sentListModel);
		sentList.setCellRenderer(new MailListCellRenderer());
		sentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sentListScrollPane = new JScrollPane(sentList);
		sentListScrollPane.setBounds(0, 34, 270, 585);
		sentListScrollPane.setBorder(null);
		sentListScrollPane.setVisible(false);
		leftPanel.add(sentListScrollPane);
		// Add listener for clicks
		sentList.addListSelectionListener(e -> {
		    if (!e.getValueIsAdjusting()) {
		        MailListItem selected = sentList.getSelectedValue();
		        if (selected != null) {
		        	String encryptedName = FileNameEncryptor.encryptFileName(selected.getTitle());
		        	File mailFile = new File("src/client/localStorage/" + accountName + "/sent/" + encryptedName + ".txt");
		            if (mailFile.exists()) {
		                displayMailDetails(mailFile, false);
		            } else {
		                JOptionPane.showMessageDialog(frame, "Mail file not found for: " + selected.getTitle(),
		                        "Missing File", JOptionPane.WARNING_MESSAGE);
		            }
		        }
		    }
		});
		
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(null);
		rightPanel.setBounds(280, 0, 906, 663);
		frame.getContentPane().add(rightPanel);
		
		JPanel header = new JPanel();
		header.setBorder(new LineBorder(new Color(0, 0, 0)));
		header.setBounds(0, 10, 896, 80);
		header.setLayout(null);
		rightPanel.add(header);
		
		header_title = new JLabel("");
		header_title.setFont(new Font("Sans Serif Collection", Font.PLAIN, 15));
		header_title.setBounds(10, 10, 876, 40);
		header.add(header_title);
		
		header_from = new JLabel("");
		header_from.setForeground(new Color(0, 0, 0));
		header_from.setFont(new Font("Sans Serif Collection", Font.PLAIN, 11));
		header_from.setBounds(10, 56, 436, 20);
		header.add(header_from);
		
		header_timestamp = new JLabel("");
		header_timestamp.setHorizontalAlignment(SwingConstants.RIGHT);
		header_timestamp.setForeground(new Color(115, 115, 115));
		header_timestamp.setFont(new Font("Sans Serif Collection", Font.PLAIN, 11));
		header_timestamp.setBounds(456, 56, 436, 20);
		header.add(header_timestamp);
		
		JPanel body = new JPanel();
		body.setBorder(new LineBorder(new Color(0, 0, 0)));
		body.setBounds(0, 100, 896, 563);
		body.setLayout(null);
		rightPanel.add(body);
		
		bodyTa = new JTextArea();
//		bodyTa.setBackground(new Color(243, 243, 243));
		bodyTa.setBackground(new Color(255, 255, 255));
		bodyTa.setLineWrap(true);
		bodyTa.setEditable(false);
		bodyTa.setBounds(0, 0, 896, 563);
		body.add(bodyTa);
	}
	
	private void refreshData() {
		requestFolderData(serverIP, serverPort);
		loadInboxList();
		loadSentList();
	}
	
	private void requestFolderData(String serverIP, String serverPort) {
		try (SmtpClient smtp = new SmtpClient(serverIP, Integer.parseInt(serverPort))) {
			smtp.requestFolderData(accountName);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Cannot reach server (cannot request account data)", "Network Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
	}
	
	public void setOnLogout(Runnable onLogout) {
	    this.onLogout = onLogout;
	}
	
	public void setOnExit(Runnable onExit) {
	    this.onExit = onExit;
	}
	
	public void showComposeUi() {
		ComposeUI composeUi = new ComposeUI(serverIP, serverPort, accountName);
		composeUi.display();
	}
	
	public void loadInboxList() {
	    inboxListModel.clear();
	    File inboxDir = new File("src/client/localStorage/" + accountName + "/inbox");

	    if (!inboxDir.exists() || !inboxDir.isDirectory()) {
	        System.err.println("Inbox folder not found: " + inboxDir.getAbsolutePath());
	        return;
	    }

	    File[] mailFiles = inboxDir.listFiles((dir, name) -> name.endsWith(".txt"));
	    if (mailFiles == null) return;

	    for (File mailFile : mailFiles) {
	        try (BufferedReader reader = new BufferedReader(new FileReader(mailFile))) {
	            Map<String, String> mailData = parseMailFile(reader);
	            String title = mailData.getOrDefault("TITLE", mailFile.getName());
	            String from = mailData.getOrDefault("FROM", "unknown@mailServer.com");
	            String timestamp = mailData.getOrDefault("TIMESTAMP",
	                    new SimpleDateFormat("HH:mm dd/MM/yyyy").format(new Date(mailFile.lastModified())));

	            addInboxListItem(title, from + "@mailServer.com", timestamp);

	        } catch (Exception e) {
	            System.err.println("Error reading mail file: " + mailFile.getName());
	            e.printStackTrace();
	        }
	    }
	}

	public void loadSentList() {
	    sentListModel.clear();
	    File sentDir = new File("src/client/localStorage/" + accountName + "/sent");

	    if (!sentDir.exists() || !sentDir.isDirectory()) {
	        System.err.println("Sent folder not found: " + sentDir.getAbsolutePath());
	        return;
	    }

	    File[] mailFiles = sentDir.listFiles((dir, name) -> name.endsWith(".txt"));
	    if (mailFiles == null) return;

	    for (File mailFile : mailFiles) {
	        try (BufferedReader reader = new BufferedReader(new FileReader(mailFile))) {
	            Map<String, String> mailData = parseMailFile(reader);
	            String title = mailData.getOrDefault("TITLE", mailFile.getName());
	            String to = mailData.getOrDefault("TO", "unknown@mailServer.com");
	            String timestamp = mailData.getOrDefault("TIMESTAMP",
	                    new SimpleDateFormat("HH:mm dd/MM/yyyy").format(new Date(mailFile.lastModified())));

	            addSentListItem(title, to + "@mailServer.com", timestamp);

	        } catch (Exception e) {
	            System.err.println("Error reading sent mail file: " + mailFile.getName());
	            e.printStackTrace();
	        }
	    }
	}

	/**
	 * Parses a mail file with structure:
	 * TITLE=...
	 * FROM=...
	 * FROM_IP=...
	 * TO=...
	 * TIMESTAMP=...
	 * CONTENT=
	 * <multiline body>
	 */
	private Map<String, String> parseMailFile(BufferedReader reader) throws Exception {
	    Map<String, String> data = new HashMap<>();
	    String line;
	    StringBuilder contentBuilder = new StringBuilder();

	    boolean contentMode = false;
	    while ((line = reader.readLine()) != null) {
	        if (contentMode) {
	            contentBuilder.append(line).append(System.lineSeparator());
	        } else if (line.startsWith("CONTENT=")) {
	            contentMode = true;
	        } else if (line.contains("=")) {
	            int eq = line.indexOf('=');
	            String key = line.substring(0, eq).trim();
	            String value = line.substring(eq + 1).trim();
	            data.put(key, value);
	        }
	    }

	    if (contentBuilder.length() > 0) {
	        data.put("CONTENT", contentBuilder.toString().trim());
	    }

	    return data;
	}
	
	private void displayMailDetails(File mailFile, boolean isInbox) {
	    try (BufferedReader reader = new BufferedReader(new FileReader(mailFile))) {
	        Map<String, String> mailData = parseMailFile(reader);

	        String title = mailData.getOrDefault("TITLE", "(No Title)");
	        String from = mailData.getOrDefault("FROM", "unknown@mailServer.com");
	        String to = mailData.getOrDefault("TO", "unknown@mailServer.com");
	        String timestamp = mailData.getOrDefault("TIMESTAMP", "Unknown Date");
	        String content = mailData.getOrDefault("CONTENT", "(No Content)");

	        header_title.setText(title);
	        header_from.setText(isInbox ? ("From: " + from + "@mailServer.com") : ("To: " + to + "@mailServer.com"));
	        header_timestamp.setText(timestamp);
	        bodyTa.setText(content);

	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(frame, "Error reading email file: " + mailFile.getName(),
	                "File Read Error", JOptionPane.ERROR_MESSAGE);
	        e.printStackTrace();
	    }
	}
	
	public void addInboxListItem(String title, String from, String timestamp) {
		inboxListModel.addElement(new MailListItem(title, from, timestamp));
	}
	
	public void addSentListItem(String title, String from, String timestamp) {
		sentListModel.addElement(new MailListItem(title, from, timestamp));
	}
	
	public void handleLogout() {	
	    int confirm = JOptionPane.showConfirmDialog(
	        frame,
	        "Are you sure you want to log out?",
	        "Confirm Logout",
	        JOptionPane.YES_NO_OPTION
	    );

	    if (confirm == JOptionPane.YES_OPTION) {   	
	        frame.dispose(); 
	        if (onLogout != null) {
	            onLogout.run(); // Trigger callback
	        }
	    }
	}
	
	public void handleExit() {
	    int confirm = JOptionPane.showConfirmDialog(
	        frame,
	        "Are you sure you want to log out and exit the program?",
	        "Confirm Logout",
	        JOptionPane.YES_NO_OPTION
	    );

	    if (confirm == JOptionPane.YES_OPTION) {   	
	        frame.dispose(); 
	        if (onExit != null) {
	            onExit.run(); // Trigger callback
	        }
	    }
	}

}
