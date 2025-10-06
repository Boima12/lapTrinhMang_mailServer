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
		
		// delete later
		addInboxListItem("This is inbox list", "Boima@mailServer.com", "5:46 06/10/2025");
		addInboxListItem("title sample", "Boima@mailServer.com", "5:46 06/10/2025");
		addSentListItem("This is sent list", "Boima@mailServer.com", "5:46 06/10/2025");
		addSentListItem("title sample", "Boima@mailServer.com", "5:46 06/10/2025");
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
		            System.out.println("Clicked item:");
		            System.out.println("Title: " + selected.getTitle());
		            System.out.println("From: " + selected.getFrom());
		            System.out.println("Timestamp: " + selected.getTimestamp() + "\n");
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
		            System.out.println("Clicked item:");
		            System.out.println("Title: " + selected.getTitle());
		            System.out.println("From: " + selected.getFrom());
		            System.out.println("Timestamp: " + selected.getTimestamp() + "\n");
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
	
	public void showComposeUi() {
		ComposeUI composeUi = new ComposeUI();
		composeUi.display();
	}
	
	public void addInboxListItem(String title, String from, String timestamp) {
		inboxListModel.addElement(new MailListItem(title, from, timestamp));
	}
	
	public void addSentListItem(String title, String from, String timestamp) {
		sentListModel.addElement(new MailListItem(title, from, timestamp));
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
