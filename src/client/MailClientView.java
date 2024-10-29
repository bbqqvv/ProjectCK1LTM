package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class MailClientView extends JFrame {
    private JPanel sidePanel, mainPanel;
    private MailClient client;
    private String username; // Đây sẽ là username hoặc email
    private JTextArea sendEmailContentArea; // JTextArea cho gửi email
    private JTextArea loadEmailContentArea; // JTextArea cho tải email
    private JLabel statusLabel;

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

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchEmail(searchField.getText()));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        panel.add(searchPanel, BorderLayout.NORTH);

        loadEmailContentArea = new JTextArea(15, 30);
        panel.add(new JScrollPane(loadEmailContentArea), BorderLayout.CENTER);

        JButton loadButton = new JButton("📥 Load Emails");
        loadButton.addActionListener(e -> loadEmails());
        panel.add(loadButton, BorderLayout.SOUTH);

        return panel;
    }

    private void loadEmails() {
        try {
            String response = client.sendRequest("LOAD_EMAILS:" + username);
            String[] emails = response.split(";");
            DefaultListModel<String> listModel = new DefaultListModel<>();
            Map<String, String> emailDetailsMap = new HashMap<>();

            for (String email : emails) {
                String[] fields = email.split(",");
                if (fields.length >= 6) {
                    String id = fields[0].split(":")[1].trim();
                    String sender = fields[1].split(":")[1].trim();
                    String subject = fields[3].split(":")[1].trim();

                    String overview = "Sender: " + sender + " | Subject: " + subject;
                    listModel.addElement(overview);

                    emailDetailsMap.put(overview, email);
                }
            }

            JList<String> emailList = new JList<>(listModel);
            emailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            emailList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    String selectedEmail = emailList.getSelectedValue();
                    if (selectedEmail != null) {
                        showEmailDetails(emailDetailsMap.get(selectedEmail));
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(emailList);
            mainPanel.add(scrollPane, "EmailOverview");
            switchPanel("EmailOverview");

            updateStatusLabel("Emails loaded.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void showEmailDetails(String email) {
        String[] fields = email.split(",");
        StringBuilder details = new StringBuilder();
        for (String field : fields) {
            details.append(field.trim()).append("\n");
        }
        JOptionPane.showMessageDialog(this, details.toString(), "Email Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void searchEmail(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            try {
                String response = client.sendRequest("SEARCH_EMAILS:" + username + ":" + keyword);
                loadEmailContentArea.setText(response);
                updateStatusLabel("Search results for: " + keyword);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
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
                loadEmailContentArea.setText(response);
                updateStatusLabel("Search results for: " + keyword);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}
