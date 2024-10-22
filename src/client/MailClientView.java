package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MailClientView extends JFrame {
    private JPanel sidePanel; // Sidebar chứa các nút
    private JPanel mainPanel; // Panel chính để hiển thị nội dung
    private MailClient client;
    private String username;

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

        sidePanel.add(sendEmailButton);
        sidePanel.add(loadEmailsButton);
        sidePanel.add(deleteEmailButton);

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

        setVisible(true);
    }

    private void switchPanel(String panelName) {
        CardLayout cl = (CardLayout) (mainPanel.getLayout());
        cl.show(mainPanel, panelName);
    }

    private JPanel createSendEmailPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(255, 255, 255)); // Màu trắng

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(0, 2, 10, 10)); // Sử dụng GridLayout với khoảng cách
        inputPanel.add(new JLabel("Receiver:"));
        JTextField receiverField = new JTextField(20);
        inputPanel.add(receiverField);

        inputPanel.add(new JLabel("Subject:"));
        JTextField subjectField = new JTextField(20);
        inputPanel.add(subjectField);

        panel.add(inputPanel, BorderLayout.NORTH);

        JTextArea emailContentArea = new JTextArea(10, 30);
        panel.add(new JScrollPane(emailContentArea), BorderLayout.CENTER);

        JButton sendButton = new JButton("Send Email");
        sendButton.addActionListener(e -> {
            String receiver = receiverField.getText();
            String subject = subjectField.getText();
            String content = emailContentArea.getText();
            try {
                String response = client.sendRequest("SEND_EMAIL:" + username + ":" + receiver + ":" + subject + ":" + content);
                JOptionPane.showMessageDialog(this, response);
            } catch (Exception ex) {
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

        JTextArea emailContentArea = new JTextArea(15, 30);
        panel.add(new JScrollPane(emailContentArea), BorderLayout.CENTER);

        JButton loadButton = new JButton("Load Emails");
        loadButton.addActionListener(e -> {
            try {
                String response = client.sendRequest("LOAD_EMAILS:" + username);
                emailContentArea.setText(response);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        panel.add(loadButton, BorderLayout.SOUTH);
        return panel;
    }

    private void deleteEmail() {
        // Thêm logic xóa email ở đây
        // Ví dụ: hiển thị danh sách email và cho phép người dùng chọn email để xóa
        JOptionPane.showMessageDialog(this, "Chức năng xóa email chưa được thực hiện.");
    }
}
