package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

public class MailClientView extends JFrame {
    private JPanel sidePanel, mainPanel;
    private MailClient client;
    private String username; // Tên người dùng hoặc email
    private JTextArea sendEmailContentArea;
    private JTextArea emailDetailsArea;
    private JLabel statusLabel;
    private DefaultTableModel emailTableModel;
    private JTable emailTable;
    private int currentPage = 1;
    private final int emailsPerPage = 10;
    private Map<Integer, String[]> emailCache = new HashMap<>();
    private List<String> emailContents = new ArrayList<>();

	private JTextArea loadEmailContentArea;

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

        createSidePanel();
        createMainPanel();
        createStatusLabel();

        setVisible(true);
        updateStatusLabel("Logged in as: " + username);
    }

    private void createSidePanel() {
        sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(new Color(245, 245, 245));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addButtonToSidePanel("✉ Send Email", e -> switchPanel("SendEmail"));
        addButtonToSidePanel("📥 Load Emails", e -> switchPanel("LoadEmails"));
        addButtonToSidePanel("🗑️ Delete Email", e -> deleteEmail());
        addButtonToSidePanel("↩️ Reply Email", e -> replyEmail());
        addButtonToSidePanel("🔍 Search Email", e -> searchEmail());

        getContentPane().add(sidePanel, BorderLayout.WEST);
    }

    private void addButtonToSidePanel(String text, ActionListener action) {
        JButton button = createButton(text);
        button.addActionListener(action);
        sidePanel.add(button);
        sidePanel.add(Box.createVerticalStrut(10));
    }

    private void createMainPanel() {
        mainPanel = new JPanel(new CardLayout());
        mainPanel.add(createSendEmailPanel(), "SendEmail");
        mainPanel.add(createLoadEmailsPanel(), "LoadEmails");
        getContentPane().add(mainPanel, BorderLayout.CENTER);
    }

    private void createStatusLabel() {
        statusLabel = new JLabel("", JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(173, 216, 230));
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        getContentPane().add(statusLabel, BorderLayout.SOUTH);
    }

    private void updateStatusLabel(String message) {
        statusLabel.setText(message);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 40));
        button.setFocusPainted(false);
        button.setBackground(new Color(173, 216, 230));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorderPainted(false);
        button.addMouseListener(new ButtonHoverEffect());
        button.setToolTipText("Click to " + text);
        return button;
    }

    private class ButtonHoverEffect extends MouseAdapter {
        public void mouseEntered(MouseEvent evt) {
            ((JButton) evt.getSource()).setBackground(new Color(100, 149, 237));
        }

        public void mouseExited(MouseEvent evt) {
            ((JButton) evt.getSource()).setBackground(new Color(173, 216, 230));
        }
    }

    private void switchPanel(String panelName) {
        CardLayout cl = (CardLayout) (mainPanel.getLayout());
        cl.show(mainPanel, panelName);
        updateStatusLabel("You are viewing: " + panelName);
        
        if (panelName.equals("LoadEmails")) {
            loadEmails(1); // Tự động tải trang đầu tiên khi vào LoadEmails panel
        }
    }

    private JPanel createSendEmailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        JTextField receiverField = new JTextField(20);
        JTextField subjectField = new JTextField(20);

        addInputField(inputPanel, "Receiver (Username/Email):", receiverField);
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
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String response = client.sendRequest("SEND_EMAIL:" + username + ":" + receiver + ":" + subject + ":" + content);
            JOptionPane.showMessageDialog(this, response);
            updateStatusLabel("Email sent to " + receiver);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private JPanel createLoadEmailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 248, 255));

        emailTableModel = new DefaultTableModel(new String[]{"ID", "Sender", "Subject", "Date"}, 0);
        emailTable = new JTable(emailTableModel);
        emailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showEmailDetails();
            }
        });
        emailTable.setDefaultEditor(Object.class, null); // Make table read-only
        emailTable.setFillsViewportHeight(true);
        emailTable.setRowHeight(30); // Increase row height
        emailTable.getTableHeader().setReorderingAllowed(false); // Disable column reordering

        JScrollPane emailScrollPane = new JScrollPane(emailTable);
        panel.add(emailScrollPane, BorderLayout.CENTER);

        // Create and initialize emailDetailsArea
        emailDetailsArea = new JTextArea(5, 30);
        emailDetailsArea.setEditable(false); // Make it read-only
        emailDetailsArea.setLineWrap(true);
        emailDetailsArea.setWrapStyleWord(true);
        JScrollPane detailsScrollPane = new JScrollPane(emailDetailsArea);
        panel.add(detailsScrollPane, BorderLayout.SOUTH);

        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton prevPageButton = new JButton("Previous Page");
        JButton nextPageButton = new JButton("Next Page");
        paginationPanel.add(prevPageButton);
        paginationPanel.add(nextPageButton);

        prevPageButton.addActionListener(e -> loadEmails(currentPage - 1));
        nextPageButton.addActionListener(e -> loadEmails(currentPage + 1));

        panel.add(paginationPanel, BorderLayout.NORTH); // Moved to the north for better layout
        return panel;
    }

 // Tạo một danh sách để lưu trữ nội dung email

    private void loadEmails(int page) {
        currentPage = page;
        emailTableModel.setRowCount(0); // Xóa dữ liệu cũ trong bảng
        emailContents.clear(); // Xóa danh sách nội dung cũ

        try {
            String response = client.sendRequest("LOAD_EMAILS:" + username + ":" + currentPage + ":" + emailsPerPage);

            if (response == null || response.isEmpty()) {
                updateStatusLabel("No emails to display.");
                return;
            }

            // Sử dụng split với "\n" để tách từng email
            String[] emails = response.split("\n");

            // Kiểm tra nếu đã đến trang cuối cùng
            if (emails.length < emailsPerPage && currentPage != 1) {
                JOptionPane.showMessageDialog(this, "Bạn đã đến trang cuối cùng.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }

            for (String email : emails) {
                // Kiểm tra xem chuỗi email có trống hay không trước khi phân tích
                if (email.trim().isEmpty()) continue;

                // Sử dụng regex để trích xuất các trường
                String regex = "ID: (\\d+), Sender: ([^,]+), Receiver: ([^,]+), Subject: ([^,]+), Content: (.*?), Sent Date: ([^,]+), Is Sent: (true|false)";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(email);

                if (matcher.find()) {
                    String id = matcher.group(1);
                    String sender = matcher.group(2);
                    String subject = matcher.group(4);
                    String date = matcher.group(6);
                    String content = matcher.group(5); // Lưu nội dung vào danh sách

                    // Thêm dữ liệu vào bảng
                    emailTableModel.addRow(new Object[]{id, sender, subject, date});
                    emailContents.add(content); // Thêm nội dung vào danh sách
                } else {
                    // Nếu không tìm thấy định dạng đúng, có thể ghi log hoặc thông báo
                    System.err.println("Dữ liệu không đúng định dạng: " + email);
                }
            }

            if (emailTableModel.getRowCount() == 0) {
                updateStatusLabel("No emails found for page " + currentPage + ".");
            } else {
                updateStatusLabel("Loaded " + emailTableModel.getRowCount() + " emails.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void showEmailDetails() {
        int selectedRow = emailTable.getSelectedRow();
        if (selectedRow != -1) {
            String emailDetails = "";
            for (int col = 0; col < emailTable.getColumnCount(); col++) {
                emailDetails += emailTable.getColumnName(col) + ": " + emailTable.getValueAt(selectedRow, col) + "\n";
            }
            // Thêm nội dung email vào chi tiết
            String content = emailContents.get(selectedRow); // Lấy nội dung từ danh sách
            emailDetails += "Content: " + content + "\n"; // Thêm nội dung vào chi tiết
            emailDetailsArea.setText(emailDetails);
        }
    }

    private void deleteEmail() {
        String emailId = JOptionPane.showInputDialog(this, "Enter email ID to delete:");
        if (emailId != null && !emailId.isEmpty()) {
            try {
                String response = client.sendRequest("DELETE_EMAIL:" + username + ":" + emailId);
                JOptionPane.showMessageDialog(this, response);
                updateStatusLabel("Email ID " + emailId + " deleted.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void replyEmail() {
        String emailId = JOptionPane.showInputDialog(this, "Enter email ID to reply to:");
        if (emailId != null && !emailId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Replying to email ID: " + emailId);
            updateStatusLabel("Replying to email ID: " + emailId);
        }
    }

    private void searchEmail() {
        String keyword = JOptionPane.showInputDialog(this, "Enter keyword to search:");
        if (keyword != null && !keyword.isEmpty()) {
            try {
                String response = client.sendRequest("SEARCH_EMAILS:" + username + ":" + keyword);
                // Assuming loadEmailContentArea is defined; if not, implement as needed.
                loadEmailContentArea.setText(response);
                updateStatusLabel("Search results for: " + keyword);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}
