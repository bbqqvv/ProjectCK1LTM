package client;

import javax.swing.*;
import java.awt.*;

public class MailClientView extends JFrame {
    private JPanel sidePanel; // Sidebar chứa các nút
    private JPanel mainPanel; // Panel chính để hiển thị nội dung
    private MailClient client;
    private String username;
    private JTextArea emailContentArea; // Hiển thị nội dung email

    public MailClientView(MailClient client, String username) {
        this.client = client;
        this.username = username;
        setTitle("Mail Client");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        // Tạo sidebar
        sidePanel = new JPanel();
        sidePanel.setLayout(new GridLayout(0, 1, 10, 10)); // Sắp xếp theo cột với khoảng cách giữa các nút
        sidePanel.setBackground(new Color(173, 216, 230)); // Màu xanh nhạt
        getContentPane().add(sidePanel, BorderLayout.WEST);

        // Thêm các nút vào sidebar
        JButton sendEmailButton = new JButton("Send Email");
        JButton loadEmailsButton = new JButton("Load Emails");
        JButton deleteEmailButton = new JButton("Delete Email"); // Nút xóa email
        JButton replyEmailButton = new JButton("Reply Email"); // Nút trả lời email
        JButton searchEmailButton = new JButton("Search Email"); // Nút tìm kiếm email

        sidePanel.add(sendEmailButton);
        sidePanel.add(loadEmailsButton);
        sidePanel.add(deleteEmailButton);
        sidePanel.add(replyEmailButton);
        sidePanel.add(searchEmailButton);

        // Tạo panel chính để hiển thị nội dung
        mainPanel = new JPanel();
        mainPanel.setLayout(new CardLayout()); // Sử dụng CardLayout để chuyển đổi giữa các giao diện
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        // Giao diện gửi email
        JPanel sendEmailPanel = createSendEmailPanel();
        mainPanel.add(sendEmailPanel, "SendEmail");

        // Giao diện tải email
        JPanel loadEmailsPanel = createLoadEmailsPanel();
        mainPanel.add(loadEmailsPanel, "LoadEmails");

        // Xử lý sự kiện cho các nút
        sendEmailButton.addActionListener(e -> switchPanel("SendEmail"));
        loadEmailsButton.addActionListener(e -> switchPanel("LoadEmails"));
        deleteEmailButton.addActionListener(e -> deleteEmail()); // Xử lý sự kiện xóa email
        replyEmailButton.addActionListener(e -> replyEmail()); // Xử lý sự kiện trả lời email
        searchEmailButton.addActionListener(e -> searchEmail()); // Xử lý sự kiện tìm kiếm email

        setLocationRelativeTo(null); // Căn giữa cửa sổ
        setVisible(true);
    }

    private void switchPanel(String panelName) {
        CardLayout cl = (CardLayout) (mainPanel.getLayout());
        cl.show(mainPanel, panelName);
    }

    private JPanel createSendEmailPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE); // Màu trắng

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(0, 2, 10, 10)); // Sử dụng GridLayout với khoảng cách
        inputPanel.add(new JLabel("Receiver:"));
        JTextField receiverField = new JTextField(20);
        inputPanel.add(receiverField);

        inputPanel.add(new JLabel("Subject:"));
        JTextField subjectField = new JTextField(20);
        inputPanel.add(subjectField);

        panel.add(inputPanel, BorderLayout.NORTH);

        emailContentArea = new JTextArea(10, 30);
        panel.add(new JScrollPane(emailContentArea), BorderLayout.CENTER);

        JButton sendButton = new JButton("Send Email");
        sendButton.addActionListener(e -> {
            String receiver = receiverField.getText();
            String subject = subjectField.getText();
            String content = emailContentArea.getText();
            if (receiver.isEmpty() || subject.isEmpty() || content.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String response = client.sendRequest("SEND_EMAIL:" + username + ":" + receiver + ":" + subject + ":" + content);
                JOptionPane.showMessageDialog(this, response);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        panel.add(sendButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createLoadEmailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(240, 248, 255)); // Màu xanh nhạt

        emailContentArea = new JTextArea(15, 30);
        panel.add(new JScrollPane(emailContentArea), BorderLayout.CENTER);

        JButton loadButton = new JButton("Load Emails");
        loadButton.addActionListener(e -> {
            try {
                String response = client.sendRequest("LOAD_EMAILS:" + username);
                emailContentArea.setText(response);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        panel.add(loadButton, BorderLayout.SOUTH);
        return panel;
    }

    private void deleteEmail() {
        String emailId = JOptionPane.showInputDialog(this, "Enter email ID to delete:");
        if (emailId != null && !emailId.isEmpty()) {
            try {
                String response = client.sendRequest("DELETE_EMAIL:" + username + ":" + emailId);
                JOptionPane.showMessageDialog(this, response);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void replyEmail() {
        String emailId = JOptionPane.showInputDialog(this, "Enter email ID to reply to:");
        if (emailId != null && !emailId.isEmpty()) {
            // Logic để trả lời email dựa trên emailId
            String response = "Replying to email ID: " + emailId; // Chưa thực hiện logic thực tế
            JOptionPane.showMessageDialog(this, response);
        }
    }

    private void searchEmail() {
        String keyword = JOptionPane.showInputDialog(this, "Enter keyword to search:");
        if (keyword != null && !keyword.isEmpty()) {
            try {
                String response = client.sendRequest("SEARCH_EMAILS:" + username + ":" + keyword);
                emailContentArea.setText(response);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}