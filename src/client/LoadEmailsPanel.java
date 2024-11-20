package client;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import controller.LoadEmailsController;
import model.Mail;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class LoadEmailsPanel extends JPanel {
	private JTable emailTable;
	private DefaultTableModel emailTableModel;
	private MailClientView mailClientView;
	private JTextPane emailDetailsArea;
	private List<Mail> emailList = new ArrayList<>();
	private LoadEmailsController controller;

	public LoadEmailsPanel(MailClientView parent) {
	    this.mailClientView = parent;
	    this.controller = new LoadEmailsController(this, parent.getClient(), parent.getUserEmail());
	    // Khởi tạo bảng và mô hình dữ liệu
	    emailTableModel = new DefaultTableModel();
	    emailTable = new JTable(emailTableModel);
	    emailTableModel.addColumn("ID");
	    emailTableModel.addColumn("Sender");
	    emailTableModel.addColumn("Subject");
	    emailTableModel.addColumn("Date");

	    setLayout(new BorderLayout());

	    // Khởi tạo vùng chi tiết email
	    emailDetailsArea = new JTextPane();
	    emailDetailsArea.setContentType("text/html");
	    emailDetailsArea.setEditable(false);
	    emailDetailsArea.setFont(new Font("Arial", Font.PLAIN, 14));

	    // Tạo các thành phần giao diện
	    createComponents();

	    // Tải email lần đầu tiên
	    controller.loadEmails(1); 
	}


	private void createComponents() {
		// Tạo panel tìm kiếm
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		JTextField searchField = new JTextField(20);
		JButton searchButton = new JButton("🔍 Tìm kiếm");

		searchPanel.add(new JLabel("Tìm kiếm theo Subject hoặc Sender:"));
		searchPanel.add(searchField);
		searchPanel.add(searchButton);
		add(searchPanel, BorderLayout.NORTH);

		// Cài đặt bảng
		emailTable.setRowHeight(40); // Đặt chiều cao dòng
		emailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		emailTable.setDefaultEditor(Object.class, null);
		customizeTableAppearance();

		// Thêm bảng vào giao diện
		JScrollPane emailScrollPane = new JScrollPane(emailTable);
		add(emailScrollPane, BorderLayout.CENTER);

		// Tạo vùng chi tiết và phân chia giao diện
		JScrollPane detailsScrollPane = new JScrollPane(emailDetailsArea);
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, emailScrollPane, detailsScrollPane);
		splitPane.setResizeWeight(0.7);
		add(splitPane, BorderLayout.CENTER);

		// Tạo các nút phân trang
		JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton prevPageButton = new JButton("◀ Trang trước");
		JButton nextPageButton = new JButton("Trang sau ▶");
		JLabel pageLabel = new JLabel("Trang: 1");

		paginationPanel.add(prevPageButton);
		paginationPanel.add(pageLabel);
		paginationPanel.add(nextPageButton);
		add(paginationPanel, BorderLayout.SOUTH);

		// Lắng nghe các sự kiện
		searchButton.addActionListener(e -> controller.handleSearch(searchField.getText()));
		prevPageButton.addActionListener(e -> controller.handlePagination(false));
		nextPageButton.addActionListener(e -> controller.handlePagination(true));

		// Lắng nghe sự kiện chọn dòng trong bảng
		emailTable.getSelectionModel().addListSelectionListener(e -> showEmailDetails());

		// Thêm menu chuột phải
		addRightClickMenu();
	}

	private void addRightClickMenu() {
		// Tạo popup menu cho chuột phải
		JPopupMenu popupMenu = new JPopupMenu();

		// Thêm mục "Xóa"
		JMenuItem deleteMenuItem = new JMenuItem("Xóa");
		deleteMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.handleDeleteEmail();
			}
		});

		// Thêm mục "Trả lời"
		JMenuItem replyMenuItem = new JMenuItem("Trả lời");
		replyMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mailClientView.replyEmail();
			}
		});

		popupMenu.add(deleteMenuItem);
		popupMenu.add(replyMenuItem);

		// Lắng nghe sự kiện chuột phải
		emailTable.setComponentPopupMenu(popupMenu);
	}

	private void showEmailDetails() {
	    int selectedRow = emailTable.getSelectedRow();
	    if (selectedRow >= 0) {
	        // Get the subject and sender from the selected row
	        String subject = emailTable.getValueAt(selectedRow, 2).toString();  // Assuming subject is in column 2
	        String sender = emailTable.getValueAt(selectedRow, 1).toString();   // Assuming sender is in column 1

	        // Assuming you have a list of emails (emailList) and each email has a getContent() method
	        String content = ""; // Default empty content
	        if (selectedRow < emailList.size()) {
	            content = emailList.get(selectedRow).getContent(); // Get content from the email list
	        }

	        // Build the HTML for displaying email details
	        String emailDetailsHTML = "<html><head><style>"
	                + "/* Some CSS styling */"
	                + "body { font-family: Arial, sans-serif; font-size: 14px; }"
	                + ".email-container { margin: 10px; padding: 10px; border: 1px solid #ddd; }"
	                + ".header { font-size: 18px; font-weight: bold; margin-bottom: 10px; }"
	                + ".email-info { margin-bottom: 5px; }"
	                + ".content { white-space: pre-wrap; word-wrap: break-word; }"
	                + "</style></head><body>"
	                + "<div class='email-container'>"
	                + "<div class='header'>Subject: <span class='subject'>" + subject + "</span></div>"
	                + "<div class='email-info'>From: <b>" + sender + "</b></div>"
	                + "<div class='content'>" + content + "</div>"
	                + "</div></body></html>";

	        // Display the email details in the emailDetailsArea (a JTextArea or similar component)
	        emailDetailsArea.setText(emailDetailsHTML);
	        emailDetailsArea.setCaretPosition(0);  // Scroll to the top of the email content
	    }
	}


	public DefaultTableModel getEmailTableModel() {
		return emailTableModel;
	}

	public void updateStatusLabel(String message) {
		mailClientView.updateStatusLabel(message);
	}

	public void showNotification(String message, String title, int messageType) {
		JOptionPane.showMessageDialog(this, message, title, messageType);
	}

	public int getSelectedRow() {
		return emailTable.getSelectedRow();
	}

	public String getEmailIdAtRow(int row) {
		return emailTable.getValueAt(row, 0).toString();
	}

	public String getUserEmail() {
		return mailClientView.getUserEmail();
	}

	private void customizeTableAppearance() {
	    JTableHeader header = emailTable.getTableHeader();
	    header.setFont(new Font("Arial", Font.BOLD, 14));
	    header.setBackground(new Color(230, 230, 250)); // Light purple for header
	    header.setForeground(Color.BLACK);

	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

	    for (int i = 0; i < emailTable.getColumnCount(); i++) {
	        emailTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
	    }

	    emailTable.setSelectionBackground(new Color(184, 207, 229));
	    emailTable.setSelectionForeground(Color.BLACK);
	}




	public JTable getEmailTable() {
		// TODO Auto-generated method stub
		return emailTable;
	}
	public List<Mail> getEmailList() {
	    return emailList;
	}


	public MailClientView getMailClientView() {
		// TODO Auto-generated method stub
		return mailClientView;
	}


}
