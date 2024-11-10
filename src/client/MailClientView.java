package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailClientView extends JFrame {
	private JPanel mainPanel;
	private MailClient client;
	private String username;
	private JTextArea sendEmailContentArea;
	private JTextPane emailDetailsArea;
	private JLabel statusLabel;
	private DefaultTableModel emailTableModel;
	private JTable emailTable;
	private int currentPage = 1;
	private int emailsPerPage = 10;
	private List<String> emailContents = new ArrayList<>();
	private JTableHeader header;

	public MailClientView(MailClient client, String username) {
		this.client = client;
		this.username = username;

		try {
			UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
		} catch (Exception e) {
			e.printStackTrace();
		}

		setTitle("Elegant Mail Client");
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout());

		// Tạo sidePanel từ lớp SidePanel mới
		SidebarPanel sidePanel = new SidebarPanel(this);
		getContentPane().add(sidePanel, BorderLayout.WEST);

		createMainPanel();
		createStatusLabel();

		setVisible(true);
		updateStatusLabel("Logged in as: " + username);
	}

	private void createMainPanel() {
		mainPanel = new JPanel(new CardLayout());
		mainPanel.add(createSendEmailPanel(), "SendEmail");
		mainPanel.add(createLoadEmailsPanel(), "LoadEmails");
		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}

	private JPanel createSendEmailPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.WHITE);

		JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
		JTextField receiverField = new JTextField(20);
		JTextField subjectField = new JTextField(20);

		addInputField(inputPanel, "Receiver Email:", receiverField);
		addInputField(inputPanel, "Subject:", subjectField);

		panel.add(inputPanel, BorderLayout.NORTH);

		sendEmailContentArea = new JTextArea(10, 30);
		panel.add(new JScrollPane(sendEmailContentArea), BorderLayout.CENTER);

		JButton sendButton = new JButton("📧 Send Email");
		sendButton.addActionListener(e -> sendEmail(receiverField, subjectField));
		panel.add(sendButton, BorderLayout.SOUTH);

		return panel;
	}

	private void addInputField(JPanel panel, String labelText, JTextField textField) {
		panel.add(new JLabel(labelText));
		panel.add(textField);
	}

	private void sendEmail(JTextField receiverField, JTextField subjectField) {
		String receiver = receiverField.getText();
		String subject = subjectField.getText();
		String content = sendEmailContentArea.getText();

		if (receiver.isEmpty() || subject.isEmpty() || content.isEmpty()) {
			showNotification("Please fill in all fields.", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			String response = client
					.sendRequest("SEND_EMAIL:" + username + ":" + receiver + ":" + subject + ":" + content);
			showNotification(response, "Email Sent", JOptionPane.INFORMATION_MESSAGE);
			updateStatusLabel("Email sent to " + receiver);
		} catch (Exception ex) {
			showNotification("An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
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
		header.setBackground(new Color(100, 149, 237));
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
		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// Kiểm tra nếu người dùng nhấn phím Enter
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchEmail(searchField.getText()); // Gọi hàm tìm kiếm khi nhấn Enter
				}
			}
		});

		prevPageButton.addActionListener(e -> loadEmails(currentPage - 1));
		nextPageButton.addActionListener(e -> loadEmails(currentPage + 1));
		// Thêm KeyListener để xử lý phím Home và End
		prevPageButton.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// Khi nhấn phím Home, chuyển đến trang đầu tiên
				if (e.getKeyCode() == KeyEvent.KEY_LOCATION_LEFT) {
					loadEmails(1); // Chuyển về trang đầu tiên
				}
			}
		});

		nextPageButton.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// Khi nhấn phím End, chuyển đến trang cuối cùng
				if (e.getKeyCode() == KeyEvent.KEY_LOCATION_RIGHT) {
					loadEmails(Integer.MAX_VALUE); // Hoặc tính toán trang cuối cùng
				}
			}
		});

		return panel;
	}

	private void searchEmail(String keyword) {
		// Kiểm tra từ khóa tìm kiếm
		if (keyword == null || keyword.trim().isEmpty()) {
			loadEmails(currentPage); // Nếu không có từ khóa, load lại tất cả email
			return;
		}

		emailTableModel.setRowCount(0); // Xóa dữ liệu bảng hiện tại
		emailContents.clear(); // Xóa danh sách email đã tải

		try {
			// Gửi yêu cầu tìm kiếm email
			String response = client
					.sendRequest("SEARCH_EMAILS:" + username + ":" + keyword + ":" + currentPage + ":" + emailsPerPage);

			if (response == null || response.isEmpty()) {
				updateStatusLabel("No emails found for the search.");
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

			updateStatusLabel("Found " + emailTableModel.getRowCount() + " emails for your search.");
		} catch (Exception ex) {
			showNotification("An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}

	private void createEmailDetailsArea() {
		emailDetailsArea = new JTextPane();
		emailDetailsArea.setContentType("text/html");
		emailDetailsArea.setEditable(false);
	}

	private void loadEmails(int page) {
		currentPage = page;
		emailTableModel.setRowCount(0);
		emailContents.clear();

		try {
			String response = client.sendRequest("LOAD_EMAILS:" + username + ":" + currentPage + ":" + emailsPerPage);

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

	private void showNotification(String message, String title, int messageType) {
		JOptionPane.showMessageDialog(this, message, title, messageType);
	}

	private void showEmailDetails() {
		int selectedRow = emailTable.getSelectedRow();
		if (selectedRow != -1) {
			String sender = emailTable.getValueAt(selectedRow, 1).toString();
			String subject = emailTable.getValueAt(selectedRow, 2).toString();
			String date = emailTable.getValueAt(selectedRow, 3).toString();
			String content = emailContents.get(selectedRow);

			String emailDetails = "<html><body style='font-family:Arial,sans-serif;'>" + "<h2>Subject: " + subject
					+ "</h2>" + "<p><strong>From:</strong> " + sender + "</p>" + "<p><strong>Date:</strong> " + date
					+ "</p>" + "<hr>" + "<div style='margin-top:10px;'>" + content + "</div>" + "</body></html>";

			emailDetailsArea.setText(emailDetails);
			emailDetailsArea.setCaretPosition(0);
		}
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
	    int confirm = JOptionPane.showConfirmDialog(this, "Are you sure to delete this email?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);

	    if (confirm == JOptionPane.YES_OPTION) { // Nếu người dùng xác nhận xóa
	        try {
	            // Gửi yêu cầu xóa email tới server
	            String response = client.sendRequest("DELETE_EMAIL:" + username + ":" + id);
	            
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


	void replyEmail() {
		JOptionPane.showMessageDialog(this, "Feature under construction", "Info", JOptionPane.INFORMATION_MESSAGE);
	}

	private void createStatusLabel() {
		statusLabel = new JLabel("Status: ");
		statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		getContentPane().add(statusLabel, BorderLayout.SOUTH);
	}

	private void updateStatusLabel(String message) {
		statusLabel.setText("Status: " + message);
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
		JDialog settingsDialog = new JDialog(this, "Settings", true);
		settingsDialog.setSize(300, 200);
		settingsDialog.setLocationRelativeTo(this);
		settingsDialog.setLayout(new BorderLayout());

		JPanel settingsPanel = new JPanel(new GridLayout(0, 2, 10, 10));

		// Thêm các tùy chọn cài đặt mẫu
		JLabel themeLabel = new JLabel("Select Theme:");
		JComboBox<String> themeComboBox = new JComboBox<>(new String[] { "Light", "Dark" });

		JLabel emailsPerPageLabel = new JLabel("Emails per Page:");
		JTextField emailsPerPageField = new JTextField(String.valueOf(emailsPerPage)); // Giá trị hiện tại

		settingsPanel.add(themeLabel);
		settingsPanel.add(themeComboBox);
		settingsPanel.add(emailsPerPageLabel);
		settingsPanel.add(emailsPerPageField);

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(e -> {
			// Lưu các cài đặt người dùng chọn
			String selectedTheme = (String) themeComboBox.getSelectedItem();
			int emailsPerPageValue;
			try {
				emailsPerPageValue = Integer.parseInt(emailsPerPageField.getText());
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Please enter a valid number for emails per page.", "Invalid Input",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			// Áp dụng theme và cập nhật số email trên mỗi trang
			applyTheme(selectedTheme);
			updateEmailsPerPage(emailsPerPageValue);

			settingsDialog.dispose();
		});

		settingsDialog.add(settingsPanel, BorderLayout.CENTER);
		settingsDialog.add(saveButton, BorderLayout.SOUTH);

		settingsDialog.setVisible(true);
	}

	private void updateEmailsPerPage(int emailsPerPageValue) {
		this.emailsPerPage = emailsPerPageValue; // Cập nhật giá trị emailsPerPage
		loadEmails(1); // Tải lại email từ trang đầu tiên
	}

	private void applyTheme(String selectedTheme) {
		if ("Dark".equalsIgnoreCase(selectedTheme)) {
			// Đặt màu nền và màu chữ cho theme tối
			getContentPane().setBackground(Color.DARK_GRAY);
			statusLabel.setForeground(Color.WHITE);
		} else {
			// Đặt theme sáng mặc định
			getContentPane().setBackground(Color.WHITE);
			statusLabel.setForeground(Color.BLACK);
		}

		// Cập nhật giao diện tất cả các thành phần để theme mới có hiệu lực
		SwingUtilities.updateComponentTreeUI(this);
	}

}
