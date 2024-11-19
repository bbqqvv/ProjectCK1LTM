package client;

import javax.swing.*;
import java.awt.*;

public class SendEmailPanel extends JPanel {

    private JTextField receiverField;
    private JTextField subjectField;
    private JTextArea contentArea;
    private JLabel fileNameLabel;
    private MailClient client; // Tham chiếu đến MailClient
    private String userEmail; // Tên người dùng để gửi email

    public SendEmailPanel(MailClientView parent) {
        this.client = parent.getClient(); // Nhận MailClient từ MailClientView
        this.userEmail = parent.getUserEmail(); // Nhận tên người dùng từ MailClientView

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Input fields for receiver and subject
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createTitledBorder("Email Details"));

        receiverField = new JTextField(20);
        subjectField = new JTextField(20);

        inputPanel.add(new JLabel("Receiver Email:"));
        inputPanel.add(receiverField);
        inputPanel.add(new JLabel("Subject:"));
        inputPanel.add(subjectField);

        // Attachment panel
        JPanel attachmentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        attachmentPanel.setBackground(Color.WHITE);
        attachmentPanel.setBorder(BorderFactory.createTitledBorder("Attachment"));

        JButton attachButton = new JButton("Choose File");
        fileNameLabel = new JLabel("No file chosen");
        attachButton.addActionListener(e -> chooseFileToAttach());

        attachmentPanel.add(new JLabel("Attach File:"));
        attachmentPanel.add(attachButton);
        attachmentPanel.add(fileNameLabel);

        // Content area
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createTitledBorder("Email Content"));

        contentArea = new JTextArea(10, 30);
        contentArea.setWrapStyleWord(true);
        contentArea.setLineWrap(true);
        contentPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton sendButton = new JButton("📧 Send Email");
        sendButton.addActionListener(e -> sendEmail());

        JButton clearButton = new JButton("🧹 Clear Fields");
        clearButton.addActionListener(e -> clearFields());

        buttonPanel.add(sendButton);
        buttonPanel.add(clearButton);

        // Add components to the main panel
        add(inputPanel);
        add(Box.createVerticalStrut(10));
        add(attachmentPanel);
        add(Box.createVerticalStrut(10));
        add(contentPanel);
        add(Box.createVerticalStrut(10));
        add(buttonPanel);
    }

    private void sendEmail() {
        String receiver = receiverField.getText();
        String subject = subjectField.getText();
        String content = contentArea.getText();

        // Basic validation checks
        if (receiver.isEmpty() || subject.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate email format (basic)
        if (!receiver.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Invalid Email", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Simulate sending an email
            String response = client.sendRequest("SEND_EMAIL:" + userEmail + ":" + receiver + ":" + subject + ":" + content);
            JOptionPane.showMessageDialog(this, response, "Email Sent", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void chooseFileToAttach() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            fileNameLabel.setText("Selected: " + fileChooser.getSelectedFile().getName());
        } else {
            fileNameLabel.setText("No file chosen");
        }
    }

    private void clearFields() {
        receiverField.setText("");
        subjectField.setText("");
        contentArea.setText("");
        fileNameLabel.setText("No file chosen");
    }

    public void setInitialValues(String receiver, String subject, String content) {
        receiverField.setText(receiver != null ? receiver : "");
        subjectField.setText(subject != null ? subject : "");
        contentArea.setText(content != null ? content : "");
    }
}
