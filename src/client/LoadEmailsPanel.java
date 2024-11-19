package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class LoadEmailsPanel extends JPanel {

    private MailClient client;
    private String userEmail;
    private DefaultTableModel emailTableModel;
    private JTable emailTable;
    private JTextPane emailDetailsArea;
    private JTextField searchField;
    private int currentPage = 1;
    private int emailsPerPage = 10;

   
    public LoadEmailsPanel(MailClientView parent) {
        this.client = parent.getClient();
        this.userEmail = parent.getUserEmail();

        setLayout(new BorderLayout());
        setBackground(new Color(240, 248, 255));

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("🔍 Search");
        searchButton.addActionListener(e -> searchEmails());
        searchPanel.add(new JLabel("Search by Subject or Sender:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        add(searchPanel, BorderLayout.NORTH);

        // Email table
        emailTableModel = new DefaultTableModel(new String[]{"ID", "Sender", "Subject", "Date"}, 0);
        emailTable = new JTable(emailTableModel);
        emailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailTable.getSelectionModel().addListSelectionListener(e -> showEmailDetails());
        JScrollPane emailScrollPane = new JScrollPane(emailTable);

        // Add right-click menu
        addRightClickMenu();

        // Email details
        emailDetailsArea = new JTextPane();
        emailDetailsArea.setContentType("text/html");
        emailDetailsArea.setEditable(false);
        JScrollPane detailsScrollPane = new JScrollPane(emailDetailsArea);

        // Split pane for table and details
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, emailScrollPane, detailsScrollPane);
        splitPane.setResizeWeight(0.7);
        add(splitPane, BorderLayout.CENTER);

        // Pagination panel
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton prevPageButton = new JButton("◀ Previous");
        JButton nextPageButton = new JButton("Next ▶");
        prevPageButton.addActionListener(e -> loadEmails(currentPage - 1));
        nextPageButton.addActionListener(e -> loadEmails(currentPage + 1));
        paginationPanel.add(prevPageButton);
        paginationPanel.add(nextPageButton);

        add(paginationPanel, BorderLayout.SOUTH);

        // Load the initial page of emails
        loadEmails(currentPage);
    }

    public void loadEmails(int page) {
        if (page < 1) return;

        currentPage = page;
        emailTableModel.setRowCount(0);

        try {
            String response = client.sendRequest("LOAD_EMAILS:" + userEmail + ":" + currentPage + ":" + emailsPerPage);
            if (response == null || response.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No emails to display.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] emails = response.split("\n");
            for (String email : emails) {
                String[] emailParts = parseEmail(email);
                if (emailParts != null) {
                    emailTableModel.addRow(emailParts);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading emails: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private String[] parseEmail(String email) {
        try {
            String[] parts = email.split(", ");
            String id = parts[0].split(": ")[1];
            String sender = parts[1].split(": ")[1];
            String subject = parts[3].split(": ")[1];
            String date = parts[5].split(": ")[1];
            return new String[]{id, sender, subject, date};
        } catch (Exception e) {
            return null;
        }
    }

    private void showEmailDetails() {
        int selectedRow = emailTable.getSelectedRow();
        if (selectedRow != -1) {
            String sender = emailTable.getValueAt(selectedRow, 1).toString();
            String subject = emailTable.getValueAt(selectedRow, 2).toString();
            String date = emailTable.getValueAt(selectedRow, 3).toString();
            String content = "<html><body>" +
                    "<h2>" + subject + "</h2>" +
                    "<p><strong>Sender:</strong> " + sender + "</p>" +
                    "<p><strong>Date:</strong> " + date + "</p>" +
                    "<hr>" +
                    "<p>Email content here...</p>" +
                    "</body></html>";
            emailDetailsArea.setText(content);
        }
    }

    private void searchEmails() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadEmails(currentPage);
            return;
        }

        emailTableModel.setRowCount(0);

        try {
            String response = client.sendRequest("SEARCH_EMAILS:" + userEmail + ":" + keyword + ":" + currentPage + ":" + emailsPerPage);
            if (response == null || response.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No results found.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] emails = response.split("\n");
            for (String email : emails) {
                String[] emailParts = parseEmail(email);
                if (emailParts != null) {
                    emailTableModel.addRow(emailParts);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error searching emails: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void addRightClickMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem deleteMenuItem = new JMenuItem("Delete");
        deleteMenuItem.addActionListener(e -> deleteEmail());
        popupMenu.add(deleteMenuItem);

        JMenuItem replyMenuItem = new JMenuItem("Reply");
        replyMenuItem.addActionListener(e -> replyEmail());
        popupMenu.add(replyMenuItem);

        emailTable.setComponentPopupMenu(popupMenu);
    }

    private void deleteEmail() {
        int selectedRow = emailTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an email to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String emailId = emailTable.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this email?", "Delete Email", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String response = client.sendRequest("DELETE_EMAIL:" + userEmail + ":" + emailId);
                JOptionPane.showMessageDialog(this, response, "Info", JOptionPane.INFORMATION_MESSAGE);
                loadEmails(currentPage);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting email: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void replyEmail() {
        int selectedRow = emailTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an email to reply to.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sender = emailTable.getValueAt(selectedRow, 1).toString();
        String subject = "Re: " + emailTable.getValueAt(selectedRow, 2).toString();
        String originalContent = emailDetailsArea.getText();

        // TODO: Trigger parent panel to switch to SendEmailPanel with reply content
        JOptionPane.showMessageDialog(this, "Replying to email from: " + sender, "Reply Email", JOptionPane.INFORMATION_MESSAGE);
    }
}
