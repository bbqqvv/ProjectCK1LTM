package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import controller.LoadEmailsController;
import dao.SaveDarftDao;
import dao.ServerDAO;
import dao.UserDAO;
import database.DatabaseConnection;
import model.Mail;

/**
 * Mail Client UI
 * - Handles the user interface.
 * - Delegates email-related actions to controllers and services.
 */
public class MailClientView extends JFrame {
    private JPanel mainPanel;
    private SendEmailPanel sendEmailPanel;
    private LoadEmailsPanel loadEmailsPanel;
    private LoadEmailDraftPanel loadEmailDraftPanel;

    private ChatPanel chatPanel;
    private Timer autoRefreshTimer;
    private boolean autoRefreshEnabled = false;
    private MailClient client;
    private String userEmail;
    private JLabel statusLabel;
    private LoadEmailsController loadEmailsController;
    private List<String> emailContents; // Store email contents
    private SidebarPanel sidePanel;
    private UserDAO userDAO;
    private ServerDAO serverDAO;
    private SaveDarftDao saveDarftDao;  // Declare SaveDao here

    public MailClientView(MailClient client, String userEmail, UserDAO userDAO, ServerDAO serverDAO) throws SQLException {
        this.client = client;
        this.userEmail = userEmail;
        this.userDAO = userDAO;
        this.serverDAO = serverDAO;
        this.emailContents = new ArrayList<>();

        // Initialize connection
        Connection connection = DatabaseConnection.getConnection();

        // Initialize saveDarftDao with the database connection
        this.saveDarftDao = new SaveDarftDao(connection);  // Ensure it's initialized here

        // Initialize panels after saveDarftDao is initialized
        this.loadEmailsPanel = new LoadEmailsPanel(this);
        this.loadEmailsController = new LoadEmailsController(loadEmailsPanel, client, userEmail);

        // JFrame settings
        setTitle("Mail Client");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create the main panel
        createMainPanel();

        // Add mainPanel to the frame
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        // Create sidebar and other components
        sidePanel = new SidebarPanel(this);
        getContentPane().add(sidePanel, BorderLayout.WEST);

        // Create status label
        statusLabel = new JLabel("Status: Ready");
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        getContentPane().add(statusPanel, BorderLayout.SOUTH);

        // Show frame
        setVisible(true);
        updateStatusLabel("Logged in as: " + userEmail);

        // Load emails when the panel is created
        loadEmailsController.loadEmails(1);

        // Set up auto-refresh every 5 minutes (300000ms)
        setupAutoRefresh();
    }

    // Method to get the userId from the UserDAO based on the userEmail
    public int getUserId() throws SQLException {
        return userDAO.getUserIdByEmail(userEmail); // Truy vấn userId từ cơ sở dữ liệu
    }

    // Setup auto-refresh functionality
    private void setupAutoRefresh() {
        autoRefreshTimer = new Timer(300000, e -> {
            if (autoRefreshEnabled) {
                loadEmailsController.loadEmails(1); // Reload emails on auto-refresh
                updateStatusLabel("Emails auto-refreshed at: " + new java.util.Date());
            }
        });
    }

    private void createMainPanel() throws SQLException {
        mainPanel = new JPanel(new CardLayout());
        sendEmailPanel = new SendEmailPanel(this);
        loadEmailsPanel = new LoadEmailsPanel(this);
        chatPanel = new ChatPanel(this); // Initialize chatPanel

        // Initialize loadEmailDraftPanel with the initialized saveDarftDao
        loadEmailDraftPanel = new LoadEmailDraftPanel(this, saveDarftDao);

        mainPanel.add(loadEmailDraftPanel, "SaveDraft");
        mainPanel.add(sendEmailPanel, "SendEmail");
        mainPanel.add(loadEmailsPanel, "EmailList");
        mainPanel.add(chatPanel, "Chat");
    }

    public void switchPanel(String panelName) {
        CardLayout layout = (CardLayout) mainPanel.getLayout();
        layout.show(mainPanel, panelName);

        if ("EmailList".equals(panelName)) {
            loadEmailsController.loadEmails(1);
        } else if ("SaveDraft".equals(panelName)) {
            updateStatusLabel("Switched to SaveDraft panel.");
        } else if ("Chat".equals(panelName)) {
            updateStatusLabel("Switched to Chat panel.");
        }
    }


    // Show notifications to the user
    public void showNotification(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    // Update the status label at the bottom of the UI
    public void updateStatusLabel(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    // Method to handle deleting an email by calling the controller
    public void deleteEmail() {
        loadEmailsController.handleDeleteEmail();
    }

    // Method to handle replying to an email by calling the controller
    public void replyEmail() {
        loadEmailsController.handleReplyEmail();
    }

    // Enable or disable auto-refresh
    public void setAutoRefreshEnabled(boolean enabled) {
        this.autoRefreshEnabled = enabled;
        if (enabled) {
            autoRefreshTimer.start();
            updateStatusLabel("Auto-refresh enabled.");
        } else {
            autoRefreshTimer.stop();
            updateStatusLabel("Auto-refresh disabled.");
        }
    }

    // Open settings panel for the user
    public void openSettings() {
        SettingsDialog settingsDialog = new SettingsDialog(this, userDAO);
        settingsDialog.setVisible(true);
    }

    // Handle search emails based on the query
    public void handleSearch(String query) {
        loadEmailsController.handleSearch(query);
    }

    // Set the number of emails per page
    public void setEmailsPerPage(int emailsPerPage) {
        loadEmailsController.setEmailsPerPage(emailsPerPage);
    }

    // Set the sorting order for emails
    public void setSortOrder(String sortOrder) {
        loadEmailsController.setSortOrder(sortOrder);
    }

    // Set the notifications enabled or disabled
    public void setNotificationsEnabled(boolean enabled) {
        loadEmailsController.setNotificationsEnabled(enabled);
    }

    // Getter for the email contents list
    public List<String> getEmailContents() {
        return new ArrayList<>(emailContents);
    }

    // Setter for email contents list
    public void setEmailContents(List<String> emailContents) {
        this.emailContents = new ArrayList<>(emailContents);
    }

    // Get the MailClient instance
    public MailClient getClient() {
        return client;
    }

    // Get the user email address
    public String getUserEmail() {
        return this.userEmail;
    }

    public void setUsername(String newUsername) {
        this.userEmail = newUsername;
        updateStatusLabel("Username updated to: " + newUsername);
    }

    // Handle logout and show login screen
    public void showLoginScreen() {
        setVisible(false); // Hide current window

        try {
            Connection connection = DatabaseConnection.getConnection();
            ServerDAO newServerDAO = new ServerDAO(connection);
            new LoginView(newServerDAO, client).setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error reconnecting to the database: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Show reply email panel
    public void showReplyEmailPanel(Mail selectedMail) throws SQLException {
        SendEmailPanel replyPanel = new SendEmailPanel(this);

        // Prefill email reply information
        replyPanel.setReceiver(selectedMail.getSender());
        replyPanel.setSubject("Re: " + selectedMail.getSubject());
        replyPanel.setContent("\n\n--- Original Message ---\n" +
                "From: " + selectedMail.getSender() + "\n" +
                "Subject: " + selectedMail.getSubject() + "\n\n" +
                selectedMail.getContent());

        // Add reply panel to CardLayout and switch to it
        mainPanel.add(replyPanel, "ReplyEmail");
        switchPanel("ReplyEmail");
    }
}
