package client.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.Image;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class ComposeUI {

	private JFrame frame;
	private JTextField titleTf;
	private JTextField toAddressTf;
	private JTextArea textArea;
	private JButton btnSend;
	private JButton discardBt;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(new FlatLightLaf());
					
					ComposeUI window = new ComposeUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ComposeUI() {
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
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setTitle("Write a mail");
		frame.getContentPane().setLayout(null);
		
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
		frame.getContentPane().add(btnSend);
		
		discardBt = new JButton("");
		discardBt.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		discardBt.setBounds(1006, 95, 40, 35);
		discardBt.setIcon(new ImageIcon(new ImageIcon(ComposeUI.class.getResource("../assets/discardMessage.png")).getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
		frame.getContentPane().add(discardBt);
	}
	
//	public void refreshFields() {
//		titleTf.setText("");
//		toAddressTf.setText("");
//		textArea.setText("");
//	}
}
