package client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import dao.ServerDAO;
import dao.UserDAO;
import database.DatabaseConnection;

public class MailClientView extends JFrame {

	private Timer autoRefreshTimer;
	private boolean autoRefreshEnabled = false;
	private JPanel mainPanel;
	private MailClient client;
	private String userEmail;
	private JTextPane emailDetailsArea;
	private JLabel statusLabel;
	private DefaultTableModel emailTableModel;
	private JTable emailTable;
	private int currentPage = 1;
	private int emailsPerPage = 10;
	private List<String> emailContents = new ArrayList<>();
	private JTableHeader header;
	private String sortOrder;
	private boolean notificationsEnabled;
	private JProgressBar loadingProgressBar; // Declare it at the class level
	private JPanel panel; // Declare the panel to hold the progress bar and other components
	private JFileChooser fileChooser;
	private JButton buttonMenu;
	private SidebarPanel sidePanel;
	private SendEmailPanel sendEmailPanel;
	private boolean isSidebarVisible = true; // Trạng thái hiển thị sidebar
	private UserDAO userDAO;
	private ServerDAO serverDAO;
	public MailClientView(MailClient client, String userEmail, UserDAO userDAO, ServerDAO serverDAO) {

	    // Initialize auto-refresh timer for emails
	    autoRefreshTimer = new Timer(30000, e -> {
	        if (autoRefreshEnabled) {
	            loadEmails(currentPage); // Reload emails if auto-refresh is enabled
	        }
	    });
	    autoRefreshTimer.start();

	    this.client = client;
	    this.userEmail = userEmail;
	    this.userDAO = userDAO;
	    this.serverDAO = serverDAO;
	    this.emailsPerPage = emailsPerPage;
	    try {
	        UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    setTitle("Elegant Mail Client");
	    setSize(1100, 700);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setLocationRelativeTo(null);
	    getContentPane().setLayout(new BorderLayout());
//
//	    // Create and add the top menu panel
//	    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Align left
//	    createButtonMenu(); // Initialize buttonMenu
//	    topPanel.add(buttonMenu); // Add buttonMenu to top panel
//	    getContentPane().add(topPanel, BorderLayout.NORTH); // Add top panel to the top

	    // Create and add the sidebar panel
	    sidePanel = new SidebarPanel(this);
	    getContentPane().add(sidePanel, BorderLayout.WEST);

	    // Create main panel and status label
	    createMainPanel();
	    createStatusLabel();

	    setVisible(true);
	    updateStatusLabel("Logged in as: " + userEmail);
	}

	private void createMainPanel() {
	    mainPanel = new JPanel(new CardLayout());

	    // Sử dụng lớp mới tách riêng
	    sendEmailPanel = new SendEmailPanel(this);
	    mainPanel.add(sendEmailPanel, "SendEmail");
	    mainPanel.add(createLoadEmailsPanel(), "LoadEmails");
	    getContentPane().add(mainPanel, BorderLayout.CENTER);
	}


	private JPanel createLoadEmailsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(new Color(240, 248, 255));

		// Panel chứa các nút tìm kiếm và ô nhập
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		JTextField searchField = new JTextField(20); // Ô nhập từ khóa tìm kiếm
		JButton searchButton = new JButton("🔍 Search");

		searchPanel.add(new JLabel("Search by Subject or Sender:"));
		searchPanel.add(searchField);
		searchPanel.add(searchButton);

		panel.add(searchPanel, BorderLayout.NORTH);

		emailTableModel = new DefaultTableModel(new String[] { "ID", "Sender", "Subject", "Date" }, 0);
		emailTable = new JTable(emailTableModel);
		emailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		emailTable.setDefaultEditor(Object.class, null);
		emailTable.setRowHeight(30);
		emailTable.getTableHeader().setReorderingAllowed(false);
		emailTable.setSelectionBackground(new Color(230, 230, 250));  // Màu nền sáng khi chọn
		emailTable.setSelectionForeground(Color.BLACK);  // Màu chữ tối khi chọn

		emailTable.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				int selectedRow = emailTable.getSelectedRow();
				if (selectedRow != -1) {
					// Lấy ID của email được chọn
					String emailId = emailTable.getValueAt(selectedRow, 0).toString();
					// Lưu ID email để xóa khi cần
					System.out.println("Email selected for deletion: " + emailId);
				}
			}
		});

		header = emailTable.getTableHeader();
		header.setBackground(new Color(0, 0, 2));
		header.setForeground(Color.WHITE);
		header.setFont(new Font("Arial", Font.BOLD, 14));

		JScrollPane emailScrollPane = new JScrollPane(emailTable);
		panel.add(emailScrollPane, BorderLayout.CENTER);

		createEmailDetailsArea();
		JScrollPane detailsScrollPane = new JScrollPane(emailDetailsArea);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, emailScrollPane, detailsScrollPane);
		splitPane.setResizeWeight(0.7);
		panel.add(splitPane, BorderLayout.CENTER);

		JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton prevPageButton = new JButton("◀ Previous");
		JButton nextPageButton = new JButton("Next ▶");

		paginationPanel.add(prevPageButton);
		paginationPanel.add(nextPageButton);

		panel.add(paginationPanel, BorderLayout.SOUTH);

		emailTable.getSelectionModel().addListSelectionListener(e -> showEmailDetails());

		// Tạo sự kiện cho nút tìm kiếm
		searchButton.addActionListener(e -> searchEmail(searchField.getText()));
		// Tạo sự kiện cho ô tìm kiếm khi nhấn Enter
		// Hàm KeyListener cho tìm kiếm và phân trang
		searchField.addKeyListener(new KeyAdapter() {
		    @Override
		    public void keyPressed(KeyEvent e) {
		        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
		            searchEmail(searchField.getText()); // Gọi hàm tìm kiếm
		        }
		    }
		});

		prevPageButton.addKeyListener(new KeyAdapter() {
		    @Override
		    public void keyPressed(KeyEvent e) {
		        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
		            loadEmails(currentPage - 1);  // Chuyển trang trước
		        }
		    }
		});

		nextPageButton.addKeyListener(new KeyAdapter() {
		    @Override
		    public void keyPressed(KeyEvent e) {
		        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
		            loadEmails(currentPage + 1);  // Chuyển trang tiếp theo
		        }
		    }
		});

		addRightClickMenu();

		return panel;
	}
	// Phương thức tạo JTextPane cho khu vực hiển thị chi tiết email
	private void createEmailDetailsArea() {
		emailDetailsArea = new JTextPane();
		emailDetailsArea.setContentType("text/html");
		emailDetailsArea.setEditable(false);
	}

	public void loadEmails(int page) {
		currentPage = page;
		emailTableModel.setRowCount(0);
		emailContents.clear();
			
		try {
			String response = client.sendRequest("LOAD_EMAILS:" + userEmail + ":" + currentPage + ":" + emailsPerPage);

			if (response == null || response.isEmpty()) {
				updateStatusLabel("No emails to display.");
				return;
			}

			String[] emails = response.split("\n");

			for (String email : emails) {
				if (email.trim().isEmpty())
					continue;

				String regex = "ID: (\\d+), Sender: ([^,]+), Receiver: ([^,]+), Subject: ([^,]+), Content: (.*?), Sent Date: ([^,]+), Is Sent: (true|false)";
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(email);

				if (matcher.find()) {
					String id = matcher.group(1);
					String sender = matcher.group(2);
					String subject = matcher.group(4);
					String date = matcher.group(6);
					String content = matcher.group(5);

					emailTableModel.addRow(new Object[] { id, sender, subject, date });
					emailContents.add(content);
				}
			}

			updateStatusLabel("Loaded " + emailTableModel.getRowCount() + " emails.");

			// Thêm thông báo khi tải thành công
			showNotification("Successfully loaded " + emailTableModel.getRowCount() + " emails.", "Emails Loaded",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception ex) {
			showNotification("An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}

	// Xây dựng nội dung chi tiết email dưới dạng HTML để hiển thị
	private String buildEmailDetails(String sender, String subject, String date, String content) {
		StringBuilder emailDetails = new StringBuilder();
		emailDetails.append("<html><body style='font-family:Arial,sans-serif;'>").append("<h2>Chủ đề: ")
				.append(subject != null ? subject : "No Subject").append("</h2>")
				.append("<p><strong>Người gửi:</strong> ").append(sender != null ? sender : "Unknown Sender")
				.append("</p>").append("<p><strong>Ngày gửi:</strong> ").append(date != null ? date : "Unknown Date")
				.append("</p>").append("<hr>").append("<div style='margin-top:10px;'>")
				.append(content != null ? content : "No content available").append("</div>").append("</body></html>");
		return emailDetails.toString();
	}

	private void showEmailDetails() {
		int selectedRow = emailTable.getSelectedRow();
		if (selectedRow != -1 && selectedRow < emailContents.size()) { // Thêm kiểm tra giới hạn
			// Kiểm tra giá trị của mỗi cột để tránh lỗi NullPointerException
			Object senderObj = emailTable.getValueAt(selectedRow, 1);
			Object subjectObj = emailTable.getValueAt(selectedRow, 2);
			Object dateObj = emailTable.getValueAt(selectedRow, 3);

			String sender = (senderObj != null) ? senderObj.toString() : "Unknown Sender";
			String subject = (subjectObj != null) ? subjectObj.toString() : "No Subject";
			String date = (dateObj != null) ? dateObj.toString() : "Unknown Date";

			String content = (selectedRow < emailContents.size()) ? emailContents.get(selectedRow)
					: "No content available";

			// Tạo nội dung email để hiển thị
			String emailDetails = buildEmailDetails(sender, subject, date, content);
			emailDetailsArea.setText(emailDetails);
			emailDetailsArea.setCaretPosition(0);
		} else {
			emailDetailsArea.setText("<html><body><p>Không có chi tiết email để hiển thị.</p></body></html>");
		}
	}

	private void searchEmail(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			loadEmails(currentPage);
			return;
		}

		emailTableModel.setRowCount(0);
		emailContents.clear();

		SwingWorker<Void, String[]> searchWorker = new SwingWorker<>() {
			@Override
			protected Void doInBackground() throws Exception {
				String response = client.sendRequest(
						"SEARCH_EMAILS:" + userEmail + ":" + keyword + ":" + currentPage + ":" + emailsPerPage);

				if (response == null || response.isEmpty()) {
					publish(new String[] { "No emails found for the search." });
					return null;
				}

				String[] emails = response.split("\n");
				for (String email : emails) {
					if (email.trim().isEmpty())
						continue;
					publish(parseEmailData(email));
				}
				return null;
			}

			@Override
			protected void process(List<String[]> chunks) {
				for (String[] emailData : chunks) {
					emailTableModel.addRow(emailData);
				}
				updateStatusLabel("Found " + emailTableModel.getRowCount() + " emails for your search.");
			}

			@Override
			protected void done() {
				if (emailTableModel.getRowCount() == 0) {
					updateStatusLabel("No emails found for the search.");
				}
			}
		};
		searchWorker.execute();
	}

	private String[] parseEmailData(String email) {
		String regex = "ID: (\\d+), Sender: ([^,]+), Receiver: ([^,]+), Subject: ([^,]+), Content: (.*?), Sent Date: ([^,]+), Is Sent: (true|false)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(email);

		if (matcher.find()) {
			String id = matcher.group(1);
			String sender = matcher.group(2);
			String subject = matcher.group(4);
			String date = matcher.group(6);
			String content = matcher.group(5);
			emailContents.add(content);
			return new String[] { id, sender, subject, date };
		}
		return null;
	}


	private void showNotification(String message, String title, int messageType) {

		// Optional: Play a sound for notification
		// Optional: Change icon in system tray or app window for notifications
		JOptionPane.showMessageDialog(this, message, title, messageType);
	}

	public void updateStatusLabel(String message) {
		statusLabel.setText(message);
	}

	public void switchPanel(String panelName) {
		CardLayout layout = (CardLayout) mainPanel.getLayout();
		layout.show(mainPanel, panelName);

		if ("LoadEmails".equals(panelName)) {
			loadEmails(1);
		}
	}

	public void deleteEmail() {
		int selectedRow = emailTable.getSelectedRow(); // Lấy dòng được chọn trong bảng
		if (selectedRow == -1) {
			showNotification("Select an email to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
			return; // Nếu không có email nào được chọn, dừng lại và hiển thị cảnh báo
		}

		String id = emailTable.getValueAt(selectedRow, 0).toString(); // Lấy ID của email
		int confirm = JOptionPane.showConfirmDialog(this, "Are you sure to delete this email?", "Confirm Deletion",
				JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) { // Nếu người dùng xác nhận xóa
			try {
				// Gửi yêu cầu xóa email tới server
				String response = client.sendRequest("DELETE_EMAIL:" + userEmail + ":" + id);

				// Hiển thị thông báo kết quả xóa email
				showNotification(response, "Email Deletion", JOptionPane.INFORMATION_MESSAGE);

				// Sau khi xóa thành công, cập nhật lại bảng email
				loadEmails(currentPage); // Tải lại email cho trang hiện tại

				// Clear selection for better UX
				emailTable.clearSelection();

			} catch (Exception ex) {
				// Xử lý lỗi nếu có
				showNotification("An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		}
	}

	public void replyEmail() {
	    int selectedRow = emailTable.getSelectedRow(); // Kiểm tra email được chọn
	    if (selectedRow == -1) {
	        showNotification("Select an email to reply to.", "Warning", JOptionPane.WARNING_MESSAGE);
	        return;
	    }

	    // Lấy thông tin từ bảng email
	    String sender = emailTable.getValueAt(selectedRow, 1).toString(); // Người gửi
	    String subject = "Re: " + emailTable.getValueAt(selectedRow, 2).toString(); // Thêm "Re:" vào chủ đề
	    String originalContent = emailContents.get(selectedRow); // Nội dung email gốc
	    String date = emailTable.getValueAt(selectedRow, 3).toString(); // Ngày gửi

	    // Trích dẫn nội dung email gốc
	    String quotedContent = buildQuotedContent(sender, date, originalContent);

	    // Tạo panel mới cho trả lời email, truyền thông tin đã chuẩn bị
	    SendEmailPanel replyPanel = new SendEmailPanel(this);
	    replyPanel.setInitialValues(sender, subject, quotedContent);

	    // Thêm vào `mainPanel` và chuyển sang giao diện trả lời email
	    mainPanel.add(replyPanel, "ReplyEmail");
	    switchPanel("ReplyEmail");
	}


	private String buildQuotedContent(String sender, String date, String originalContent) {
	    // Tạo nội dung trích dẫn đẹp hơn với người gửi và ngày gửi
	    StringBuilder quotedContent = new StringBuilder();
	    quotedContent.append("<br><br>--- Original Message ---<br>");
	    quotedContent.append("<strong>From:</strong> ").append(sender).append("<br>");
	    quotedContent.append("<strong>Sent:</strong> ").append(date).append("<br>");
	    quotedContent.append("<strong>Content:</strong><br>");
	    quotedContent.append(originalContent.replaceAll("(\r\n|\n)", "<br>")); // Thêm thẻ <br> để hiển thị xuống dòng
	    return quotedContent.toString();
	}

	private void addRightClickMenu() {
		JPopupMenu popupMenu = new JPopupMenu();

		// Tùy chọn "Delete"
		JMenuItem deleteMenuItem = new JMenuItem("Delete");
		deleteMenuItem.addActionListener(e -> deleteEmail());
		popupMenu.add(deleteMenuItem);

		// Tùy chọn "Reply"
		JMenuItem replyMenuItem = new JMenuItem("Reply");
		replyMenuItem.addActionListener(e -> replyEmail());
		popupMenu.add(replyMenuItem);

		JMenuItem chatMessage = new JMenuItem("Chat");
		chatMessage.addActionListener(e -> chatUser());
		popupMenu.add(chatMessage);
		// Gắn menu chuột phải vào bảng
		emailTable.setComponentPopupMenu(popupMenu);
	}

	private void chatUser() {

	}

	private void createStatusLabel() {
		statusLabel = new JLabel("Status: ");
		statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		getContentPane().add(statusLabel, BorderLayout.SOUTH);
	}

	class ButtonHoverEffect extends MouseAdapter {
		@Override
		public void mouseEntered(MouseEvent e) {
			e.getComponent().setBackground(new Color(135, 206, 250));
		}

		@Override
		public void mouseExited(MouseEvent e) {
			e.getComponent().setBackground(new Color(173, 216, 230));
		}
	}

	public void openSettings() {
		// Truyền thêm username vào SettingsDialog
		// Truyền userDAO và serverDAO vào SettingsDialog
		new SettingsDialog(this, emailsPerPage, userEmail, userDAO);
	}

	public void setEmailsPerPage(int emailsPerPage) {
		this.emailsPerPage = emailsPerPage;
	}

	public void setFont(Font font) {
		// Apply the font to the email display area (e.g., text area, labels)
		emailDetailsArea.setFont(font);
	}

	public void setSortOrder(String sortOrder) {

		// Sorting options: by date, sender, or subject
		if ("date".equalsIgnoreCase(sortOrder)) {
		} else if ("sender".equalsIgnoreCase(sortOrder)) {
		} else if ("subject".equalsIgnoreCase(sortOrder)) {
		}

		this.sortOrder = sortOrder;
		loadEmails(1);
	}

	public void setNotificationsEnabled(boolean enabled) {
		// Implement logic to enable or disable notifications
		this.notificationsEnabled = enabled;
		// Additional logic to manage notifications, e.g., update status label
		if (enabled) {
			updateStatusLabel("Notifications enabled");
		} else {
			updateStatusLabel("Notifications disabled");
		}
	}

	public void setAutoRefreshEnabled(boolean autoRefreshEnabled) {

		this.autoRefreshEnabled = autoRefreshEnabled;
		if (!autoRefreshEnabled && autoRefreshTimer.isRunning()) {
			autoRefreshTimer.stop();
		} else if (autoRefreshEnabled && !autoRefreshTimer.isRunning()) {
			autoRefreshTimer.start();
		}
		updateStatusLabel("Auto-refresh " + (autoRefreshEnabled ? "enabled" : "disabled"));

	}

	public void setUsername(String newUsername) {

	}

	public void showLoginScreen(ServerDAO serverDAO) {
	    // Ẩn màn hình hiện tại
	    this.setVisible(false);

	    // Hiển thị thông báo đăng xuất thành công
	    JOptionPane.showMessageDialog(this, "Logged out successfully.", "Logout", JOptionPane.INFORMATION_MESSAGE);

	    // Kiểm tra nếu serverDAO bị null, tạo lại từ DatabaseConnection
	    if (serverDAO == null) {
	        try {
	            Connection connection = DatabaseConnection.getConnection();
	            serverDAO = new ServerDAO(connection);  // Tạo lại ServerDAO nếu không có sẵn
	        } catch (SQLException ex) {
	            JOptionPane.showMessageDialog(this, "Error reconnecting to the database: " + ex.getMessage(),
	                    "Database Error", JOptionPane.ERROR_MESSAGE);
	            ex.printStackTrace();
	            return;
	        }
	    }

	    // Tạo lại LoginView với serverDAO và hiển thị màn hình đăng nhập
	    LoginView loginScreen = new LoginView(serverDAO); 
	    loginScreen.setVisible(true);
	}

	public MailClient getClient() {
	    return client;
	}

	public String getUserEmail() {
	    return userEmail;
	}
}