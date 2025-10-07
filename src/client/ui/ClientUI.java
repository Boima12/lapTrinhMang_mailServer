package client.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.Image;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.SoftBevelBorder;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.border.BevelBorder;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import client.model.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientUI {

	private Runnable onLogout;
	
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
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(new FlatLightLaf());

					ClientUI window = new ClientUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ClientUI() {
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
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Mail server client");
		frame.getContentPane().setLayout(null);
		
        // JmenuBar
        JMenuBar menuBar = new JMenuBar();
        JMenu menuHome = new JMenu("Home");
        menuBar.add(menuHome);
        frame.setJMenuBar(menuBar);
        
        JMenuItem menuLogout = new JMenuItem("Logout");
        menuLogout.addActionListener(e -> {
            handleLogout();
        });
        menuHome.add(menuLogout);

		JMenuItem menuSync = new JMenuItem("Sync");
		menuSync.addActionListener(e -> {
			try { if (syncFromServer != null) syncFromServer.run(); } catch (Exception ignored) {}
		});
		menuHome.add(menuSync);
		
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
        // Khi chọn thư ở Inbox -> hiển thị nội dung bên phải
		inboxList.addListSelectionListener(e -> {
		    if (!e.getValueIsAdjusting()) {
		        MailListItem selected = inboxList.getSelectedValue();
		        if (selected != null) {
                    ClientUIHelper.showMailContent(currentUser, "inbox", selected, header_title, header_from, header_timestamp, bodyTa);
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
        // Khi chọn thư ở Sent -> hiển thị nội dung bên phải
		sentList.addListSelectionListener(e -> {
		    if (!e.getValueIsAdjusting()) {
		        MailListItem selected = sentList.getSelectedValue();
		        if (selected != null) {
                    ClientUIHelper.showMailContent(currentUser, "sent", selected, header_title, header_from, header_timestamp, bodyTa);
		        }
		    }
		});
		
		
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(null);
		rightPanel.setBounds(280, 0, 906, 663);
		frame.getContentPane().add(rightPanel);
		
		JPanel header = new JPanel();
		header.setBorder(null);
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
		body.setBounds(0, 100, 896, 563);
		body.setLayout(null);
		rightPanel.add(body);
		
		bodyTa = new JTextArea();
		bodyTa.setBackground(new Color(243, 243, 243));
		bodyTa.setLineWrap(true);
		bodyTa.setEditable(false);
		bodyTa.setBounds(0, 0, 896, 563);
		body.add(bodyTa);
	}
	
	public void setOnLogout(Runnable onLogout) {
	    this.onLogout = onLogout;
	}
	
    private String currentUser = "";
    private client.core.UdpApiClient apiClient;
	private Runnable syncFromServer;

    public void setCurrentUser(String username) { this.currentUser = username; }
    public void setApi(client.core.UdpApiClient apiClient) { this.apiClient = apiClient; }
	public void setSyncFromServer(Runnable r) { this.syncFromServer = r; }

	public void showComposeUi() {
		ComposeUI composeUi = new ComposeUI();
		composeUi.setOnSend(args -> {
			String to = args[0];
			String title = args[1];
			String content = args[2];
			// delegate to a global app hook if available
			try {
                client.core.UdpApiClient api = this.apiClient;
				if (api == null) return false;
				String from = currentUser == null ? "unknown" : currentUser;
				boolean ok = api.send(from, to, title, content);
				if (ok) {
					// write to localStorage/sent and update UI immediately
					long ts = System.currentTimeMillis();
					String tsStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(ts));
					String safeTitle = title.replaceAll("[^a-zA-Z0-9._-]", "_");
					String filename = ts + "_" + safeTitle + ".txt";
                    Path root = Path.of("src", "client", "localStorage", ClientUIHelper.toLocalName(currentUser));
                    Files.createDirectories(root.resolve("sent"));
					java.util.List<String> lines = new java.util.ArrayList<>();
					lines.add("TIMESTAMP=" + tsStr);
					lines.add("FROM=" + from);
					lines.add("TO=" + to);
					lines.add("TITLE=" + title);
					lines.add("CONTENT=" + content);
                    Files.write(root.resolve("sent").resolve(filename), lines, StandardCharsets.UTF_8);
					addSentListItem(title, from, tsStr);
				}
				return ok;
			} catch (Exception e) {
				return false;
			}
		});
		composeUi.display();
	}
	
	public void addInboxListItem(String title, String from, String timestamp) {
		inboxListModel.addElement(new MailListItem(title, from, timestamp));
	}
	
	public void addSentListItem(String title, String from, String timestamp) {
		sentListModel.addElement(new MailListItem(title, from, timestamp));
	}

	public void clearLists() {
		inboxListModel.clear();
		sentListModel.clear();
	}

	
	public void handleLogout() {
		// TODO delete everything inside localStorage folder for this client (yeah we do support multiple clients)
		
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

}
