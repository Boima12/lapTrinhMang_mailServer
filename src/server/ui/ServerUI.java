package server.ui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.JTabbedPane;
import java.awt.Color;
import java.awt.Font;

import server.model.RecordListItem;
import server.model.RecordListCellRenderer;

public class ServerUI {

	private String serverIP;
	private int serverPort;
	
	private JFrame frame;
	private JTextField searchUserTf;
	private JButton searchUserBt;
	private JTextField searchTitleTf;
	private JButton searchTitleBt;
	private JTextField searchByTimeStartTf;
	private JButton searchTitleBt_1;
	private JTextField searchByTimeEndTf;
	private DefaultListModel<RecordListItem> recordListModel_search;
	private JList<RecordListItem> recordList_search;
	private JLabel totalRecordLb_search;
	private DefaultListModel<RecordListItem> recordListModel;
	private JList<RecordListItem> recordList;
	private JLabel totalRecordLb;
	

	public ServerUI(String serverIP, int serverPort) {
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		
		initialize();
		
		// delete later 
//		addRecordListItem_search("08:08:53 06/10/2025", "boima@mailServer.com -> railgunner@mailServer.com | Hello my friend");
//		addRecordListItem_search("08:08:53 06/10/2025", "boima@mailServer.com -> railgunner@mailServer.com | This is record_search list");
		
	}
	
	public void display() {
		frame.setVisible(true);
	}

	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1200, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Mail server - " + serverIP + ":" + serverPort);
		frame.getContentPane().setLayout(null);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(10, 10, 1166, 643);
		frame.getContentPane().add(tabbedPane);
		
		JPanel overview = new JPanel();
		overview.setBounds(0, 0, 1166, 643);
		overview.setLayout(null);
		tabbedPane.addTab("Overview", null, overview, null);
		
		JPanel stats = new JPanel();
		stats.setBounds(0, 0, 1156, 120);
		stats.setLayout(null);
		overview.add(stats);
		
		JPanel recordListPanel = new JPanel();
		recordListPanel.setLayout(null);
		recordListPanel.setBounds(0, 130, 1156, 486);
		overview.add(recordListPanel);
		
		totalRecordLb = new JLabel("Total record(s): 0");
		totalRecordLb.setForeground(new Color(115, 115, 115));
		totalRecordLb.setFont(new Font("Sans Serif Collection", Font.ITALIC, 12));
		totalRecordLb.setBounds(0, 0, 600, 20);
		recordListPanel.add(totalRecordLb);
		
		recordListModel = new DefaultListModel<>();
		recordList = new JList<>(recordListModel);
		recordList.setCellRenderer(new RecordListCellRenderer());
		recordList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane recordListScrollPane = new JScrollPane(recordList);
		recordListScrollPane.setBounds(0, 25, 1156, 461);
		recordListPanel.add(recordListScrollPane);
		// double click pop up detailsDialog modal
//		recordList.addMouseListener(new MouseAdapter() {
//		    @Override
//		    public void mouseClicked(MouseEvent e) {
//		        if (e.getClickCount() == 2 && !e.isConsumed()) {
//		            e.consume();
//		            int index = recordList.locationToIndex(e.getPoint());
//		            if (index >= 0) {
//		                RecordListItem selected = recordListModel.getElementAt(index);
//
//		                // For now, assume you have a dummy size
//		                String size = "27 KB"; 
//		                
//		                DetailsDialog.showDetails(
//		                    frame, 
//		                    selected.getTitle(), 
//		                    selected.getFrom(), 
//		                    selected.getTo(), 
//		                    selected.getTimestamp(), 
//		                    size
//		                );
//		            }
//		        }
//		    }
//		});
		
		JPanel searchPane = new JPanel();
		searchPane.setBounds(0, 0, 1166, 643);
		searchPane.setLayout(null);
		tabbedPane.addTab("Search", null, searchPane, null);
		
		JPanel searchs = new JPanel();
		searchs.setBounds(0, 0, 1156, 120);
		searchs.setLayout(null);
		searchPane.add(searchs);
		
		JPanel searchByNamePanel = new JPanel();
		searchByNamePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		searchByNamePanel.setBounds(0, 0, 350, 120);
		searchs.add(searchByNamePanel);
		searchByNamePanel.setLayout(null);
		
		JLabel searchUserLb = new JLabel("Search by user");
		searchUserLb.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		searchUserLb.setBounds(10, 10, 119, 24);
		searchByNamePanel.add(searchUserLb);
		
		searchUserTf = new JTextField();
		searchUserTf.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		searchUserTf.setColumns(10);
		searchUserTf.setBounds(10, 35, 330, 30);
		searchByNamePanel.add(searchUserTf);
		
		searchUserBt = new JButton("Search");
		searchUserBt.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		searchUserBt.setBounds(115, 75, 120, 30);
		searchByNamePanel.add(searchUserBt);
		
		JPanel searchByTitlePanel = new JPanel();
		searchByTitlePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		searchByTitlePanel.setLayout(null);
		searchByTitlePanel.setBounds(350, 0, 350, 120);
		searchs.add(searchByTitlePanel);
		
		JLabel searchTitleLb = new JLabel("Search by title");
		searchTitleLb.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		searchTitleLb.setBounds(10, 10, 119, 24);
		searchByTitlePanel.add(searchTitleLb);
		
		searchTitleTf = new JTextField();
		searchTitleTf.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		searchTitleTf.setColumns(10);
		searchTitleTf.setBounds(10, 35, 330, 30);
		searchByTitlePanel.add(searchTitleTf);
		
		searchTitleBt = new JButton("Search");
		searchTitleBt.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		searchTitleBt.setBounds(115, 75, 120, 30);
		searchByTitlePanel.add(searchTitleBt);
		
		JPanel searchByTimePanel = new JPanel();
		searchByTimePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		searchByTimePanel.setLayout(null);
		searchByTimePanel.setBounds(700, 0, 456, 120);
		searchs.add(searchByTimePanel);
		
		JLabel SearchByTimeLb = new JLabel("Search by time");
		SearchByTimeLb.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		SearchByTimeLb.setBounds(10, 10, 119, 24);
		searchByTimePanel.add(SearchByTimeLb);
		
		searchByTimeStartTf = new JTextField();
		searchByTimeStartTf.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		searchByTimeStartTf.setColumns(10);
		searchByTimeStartTf.setBounds(60, 37, 200, 30);
		searchByTimePanel.add(searchByTimeStartTf);
		
		searchTitleBt_1 = new JButton("Search");
		searchTitleBt_1.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		searchTitleBt_1.setBounds(326, 77, 120, 30);
		searchByTimePanel.add(searchTitleBt_1);
		
		JLabel searchByTimeStartLb = new JLabel("Start");
		searchByTimeStartLb.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		searchByTimeStartLb.setBounds(10, 40, 50, 24);
		searchByTimePanel.add(searchByTimeStartLb);
		
		searchByTimeEndTf = new JTextField();
		searchByTimeEndTf.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		searchByTimeEndTf.setColumns(10);
		searchByTimeEndTf.setBounds(60, 77, 200, 30);
		searchByTimePanel.add(searchByTimeEndTf);
		
		JLabel searchByTimeEndLb = new JLabel("End");
		searchByTimeEndLb.setFont(new Font("Sans Serif Collection", Font.PLAIN, 14));
		searchByTimeEndLb.setBounds(10, 80, 50, 24);
		searchByTimePanel.add(searchByTimeEndLb);
		
		JPanel recordListPanel_search = new JPanel();
		recordListPanel_search.setLayout(null);
		recordListPanel_search.setBounds(0, 130, 1156, 486);
		searchPane.add(recordListPanel_search);
		
		totalRecordLb_search = new JLabel("Total record(s): 0");
		totalRecordLb_search.setForeground(new Color(115, 115, 115));
		totalRecordLb_search.setFont(new Font("Sans Serif Collection", Font.ITALIC, 12));
		totalRecordLb_search.setBounds(0, 0, 600, 20);
		recordListPanel_search.add(totalRecordLb_search);
		
		recordListModel_search = new DefaultListModel<>();
		recordList_search = new JList<>(recordListModel_search);
		recordList_search.setCellRenderer(new RecordListCellRenderer());
		recordList_search.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane recordList_searchScrollPane = new JScrollPane(recordList_search);
		recordList_searchScrollPane.setBounds(0, 25, 1156, 461);
		recordListPanel_search.add(recordList_searchScrollPane);
		// double click pop up detailsDialog modal
//		recordList_search.addMouseListener(new MouseAdapter() {
//		    @Override
//		    public void mouseClicked(MouseEvent e) {
//		        if (e.getClickCount() == 2 && !e.isConsumed()) {
//		            e.consume();
//		            int index = recordList_search.locationToIndex(e.getPoint());
//		            if (index >= 0) {
//		                RecordListItem selected = recordListModel_search.getElementAt(index);
//
//		                // For now, assume you have a dummy size
//		                String size = "27 KB"; 
//		                
//		                DetailsDialog.showDetails(
//		                    frame, 
//		                    selected.getTitle(), 
//		                    selected.getFrom(), 
//		                    selected.getTo(), 
//		                    selected.getTimestamp(), 
//		                    size
//		                );
//		            }
//		        }
//		    }
//		});
	}

    public void addRecordListItem(String timestamp, String message) {
        recordListModel.addElement(new RecordListItem(timestamp, message));
        updateTotalRecordLabel();
    }
    
    public void clearRecordList() {
        recordListModel.clear();
        updateTotalRecordLabel();
    }
    
    private void updateTotalRecordLabel() {
        int count = recordListModel.getSize();
        totalRecordLb.setText("Total record(s): " + count);
    }

    public void addRecordListItem_search(String timestamp, String message) {
        recordListModel_search.addElement(new RecordListItem(timestamp, message));
        updateTotalRecordLabel_search();
    }
    
    public void clearRecordList_search() {
        recordListModel_search.clear();
        updateTotalRecordLabel_search();
    }
    
    private void updateTotalRecordLabel_search() {
        int count = recordListModel_search.getSize();
        totalRecordLb_search.setText("Total record(s): " + count);
    }
}