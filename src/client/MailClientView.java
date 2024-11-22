package client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import controller.LoadEmailsController;
import dao.ServerDAO;
import dao.UserDAO;
import database.DatabaseConnection;
import model.Mail;
import service.EmailDeleteService;
import service.EmailLoaderService;

/**
 * Mail Client UI
 * - Handles the user interface.
 * - Delegates email-related actions to controllers and services.
 */
public class MailClientView extends JFrame {
    private JPanel mainPanel;
    private SendEmailPanel sendEmailPanel;
    private LoadEmailsPanel loadEmailsPanel;
    private  ChatPanel chatPanel;
    private Timer autoRefreshTimer;
    private boolean autoRefreshEnabled = false;
    private MailClient client;
    private String userEmail;
    private JLabel statusLabel;
    private LoadEmailsController loadEmailsController;
    private List<String> emailContents;  // Store email contents
    private SidebarPanel sidePanel;
    private UserDAO userDAO;
    private ServerDAO serverDAO;

    public MailClientView(MailClient client, String userEmail, UserDAO userDAO, ServerDAO serverDAO) {
        this.client = client;
        this.userEmail = userEmail;
        this.userDAO = userDAO;
        this.serverDAO = serverDAO;
        this.loadEmailsPanel = new LoadEmailsPanel(this);
        this.loadEmailsController = new LoadEmailsController(loadEmailsPanel, client, userEmail);

        this.emailContents = new ArrayList<>();

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
        loadEmailsController.loadEmails(1); // Load emails for the first time

        // Set up auto-refresh every 5 minutes (300000ms)
        setupAutoRefresh();

        // If you want to allow users to access Settings, initialize SettingsDialog here or as needed
//         SettingsDialog settingsDialog = new SettingsDialog(this);
        // You can invoke settingsDialog from a button or menu item later in your code.
    }



    // Setup auto-refresh functionality
    private void setupAutoRefresh() {
        autoRefreshTimer = new Timer(300000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (autoRefreshEnabled) {
                    loadEmailsController.loadEmails(1); // Reload emails on auto-refresh
                    updateStatusLabel("Emails auto-refreshed at: " + new java.util.Date());
                }
            }
        });
    }


    // Create the main panel with CardLayout
    private void createMainPanel() {
        mainPanel = new JPanel(new CardLayout()); // Initialize CardLayout panel

        sendEmailPanel = new SendEmailPanel(this);
        loadEmailsPanel = new LoadEmailsPanel(this);
        chatPanel = new ChatPanel(this);  // Initialize chatPanel

        // Add panels to the main panel
        mainPanel.add(sendEmailPanel, "SendEmail");
        mainPanel.add(loadEmailsPanel, "EmailList");
        mainPanel.add(chatPanel, "Chat");
    }

    // Switch between panels (SendEmail, EmailList, or Chat)
    public void switchPanel(String panelName) {
        CardLayout layout = (CardLayout) mainPanel.getLayout();
        layout.show(mainPanel, panelName);
        if ("EmailList".equals(panelName)) {
            loadEmailsController.loadEmails(1);
        } else if ("Chat".equals(panelName)) {
            // Handle any chat-specific logic here if needed
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
    public void setAutoRefreshEnabled(boolean autoRefreshEnabled) {
        this.autoRefreshEnabled = autoRefreshEnabled;
        if (autoRefreshEnabled) {
            autoRefreshTimer.start();
        } else {
            autoRefreshTimer.stop();
        }
    }

    // Open settings panel for the user
    public void openSettings() {
        SettingsDialog settingsDialog = new SettingsDialog(this, userDAO);
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
        return emailContents;
    }

    // Setter for email contents list
    public void setEmailContents(List<String> emailContents) {
        this.emailContents = emailContents;
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
        this.userEmail = newUsername;  // Update the userEmail field
        updateStatusLabel("Username updated to: " + newUsername);  // Update status label
    }

	public UserDAO getUserDAO() {
	    return userDAO;
	}

	public ServerDAO getServerDAO() {
	    return serverDAO;
	}

	public void showLoginScreen(ServerDAO serverDAO) {
	    // Ẩn màn hình hiện tại
	    this.setVisible(false);

	    // Hiển thị thông báo đăng xuất thành công
	    JOptionPane.showMessageDialog(this, "Logged out successfully.", "Logout", JOptionPane.INFORMATION_MESSAGE);

	    if (serverDAO == null) {
	        try {
	            Connection connection = DatabaseConnection.getConnection();
	            serverDAO = new ServerDAO(connection);
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

    public void showReplyEmailPanel(Mail selectedMail) {
        // Tạo panel trả lời email
        SendEmailPanel replyPanel = new SendEmailPanel(this);

        // Điền sẵn thông tin từ email được chọn
        replyPanel.setReceiver(selectedMail.getSender());
        replyPanel.setSubject("Re: " + selectedMail.getSubject());
        replyPanel.setContent("\n\n--- Original Message ---\n" +
                "From: " + selectedMail.getSender() + "\n" +
                "Subject: " + selectedMail.getSubject() + "\n\n" +
                selectedMail.getContent());

        // Thêm panel trả lời vào CardLayout
        mainPanel.add(replyPanel, "ReplyEmail");

        // Chuyển sang panel trả lời
        switchPanel("ReplyEmail");
    }

}
