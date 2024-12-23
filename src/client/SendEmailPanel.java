package client;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.toedter.calendar.JDateChooser;
import controller.SaveDraftController;
import controller.SendEmailController;
import database.DatabaseConnection;
import service.EmailSenderService;

public class SendEmailPanel extends JPanel {

    private JTextField receiverField;
    private JTextField subjectField;
    private JTextArea contentArea;
    private JButton scheduleButton;
    private JPanel schedulePanel;
    private SendEmailController sendEmailController;
    private SaveDraftController saveDraftController;
    private Connection connection;
    private MailClientView parent;  // Thêm một trường lưu trữ parent để lấy userId

    public SendEmailPanel(MailClientView parent) throws SQLException {
        this.parent = parent;  // Lưu đối tượng parent để lấy thông tin userId
        MailClient client = parent.getClient();
        String userEmail = parent.getUserEmail();
        EmailSenderService emailSenderService = new EmailSenderService(client, userEmail);
        connection = DatabaseConnection.getConnection();  // Giả sử bạn đã có lớp DatabaseConnection

        sendEmailController = new SendEmailController(client, userEmail, emailSenderService);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(240, 248, 255));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Input fields for receiver and subject
        JPanel inputPanel = createTitledPanel("Chi tiết email", new GridLayout(2, 2, 10, 10));
        receiverField = new JTextField(25);
        subjectField = new JTextField(25);

        inputPanel.add(new JLabel("Email người nhận: "));
        inputPanel.add(receiverField);
        inputPanel.add(new JLabel("Vấn đề: "));
        inputPanel.add(subjectField);

        saveDraftController = new SaveDraftController(connection);

        // Content area
        JPanel contentPanel = createTitledPanel("Email Content", new BorderLayout());
        contentArea = new JTextArea(8, 30);
        contentArea.setWrapStyleWord(true);
        contentArea.setLineWrap(true);
        contentPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(240, 248, 255));

        JButton sendButton = new JButton("📧 Send Email");
        sendButton.addActionListener(e -> sendEmail());
        sendButton.setBackground(new Color(72, 209, 204));
        sendButton.setForeground(Color.WHITE);

        JButton saveButton = new JButton("📂 Lưu nháp");
        saveButton.addActionListener(e -> {
            try {
                saveDraft();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }); // Lắng nghe sự kiện Lưu nháp

        JButton clearButton = new JButton("🧹 Clear Fields");
        clearButton.addActionListener(e -> clearFields());
        clearButton.setBackground(new Color(255, 99, 71));
        clearButton.setForeground(Color.WHITE);

        scheduleButton = new JButton("⏰ Schedule Send");
        scheduleButton.addActionListener(e -> toggleSchedulePanelVisibility());
        scheduleButton.setBackground(new Color(255, 255, 255));
        scheduleButton.setForeground(Color.WHITE);

        // Thêm nút vào panel
        buttonPanel.add(sendButton);
        buttonPanel.add(saveButton);  // Thêm nút Lưu nháp vào hàng cùng với các nút khác
        buttonPanel.add(scheduleButton);
        buttonPanel.add(clearButton);

        // Schedule Panel for date and time selection
        schedulePanel = createTitledPanel("Schedule Email", new FlowLayout(FlowLayout.LEFT, 10, 5));
        schedulePanel.setVisible(false); // Hidden by default

        JLabel scheduleLabel = new JLabel("Select Date and Time:");
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm:ss"));

        schedulePanel.add(scheduleLabel);
        schedulePanel.add(dateChooser);
        schedulePanel.add(timeSpinner);

        // Add components to the main panel
        add(inputPanel);
        add(Box.createVerticalStrut(10));
        add(contentPanel);
        add(Box.createVerticalStrut(10));
        add(schedulePanel);
        add(Box.createVerticalStrut(10));
        add(buttonPanel);
    }

    // Phương thức lưu email nháp
    private void saveDraft() throws SQLException {
        String receiver = receiverField.getText();
        String subject = subjectField.getText();
        String content = contentArea.getText();

        int userId = parent.getUserId();  // Giả sử bạn có phương thức getUserId trong MailClientView

        JLabel statusLabel = new JLabel(); // Hiển thị trạng thái lưu
        saveDraftController.saveDraft(receiver, subject, content, userId); // Truyền userId vào
        JOptionPane.showMessageDialog(this, statusLabel.getText(), "Lưu Thành Công", JOptionPane.INFORMATION_MESSAGE);
    }

    // Phương thức gửi email
    private void sendEmail() {
        String receiver = receiverField.getText();
        String subject = subjectField.getText();
        String content = contentArea.getText();
        JLabel statusLabel = new JLabel(); // Hiển thị trạng thái gửi

        sendEmailController.sendEmail(receiver, subject, content, statusLabel);
        JOptionPane.showMessageDialog(this, statusLabel.getText(), "Status", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createTitledPanel(String title, LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1, true),
                title
        ));
        return panel;
    }

    private void toggleSchedulePanelVisibility() {
        boolean isVisible = !schedulePanel.isVisible();
        schedulePanel.setVisible(isVisible);
        scheduleButton.setText(isVisible ? "❌ Cancel Schedule" : "⏰ Schedule Send");
        revalidate();
        repaint();
    }

    private void clearFields() {
        receiverField.setText("");
        subjectField.setText("");
        contentArea.setText("");
    }

    public void setReceiver(String receiver) {
        receiverField.setText(receiver);
    }

    public void setSubject(String subject) {
        subjectField.setText(subject);
    }

    public void setContent(String content) {
        contentArea.setText(content);
    }
}
