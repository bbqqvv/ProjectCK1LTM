package client;

import dao.ServerDAO;
import dao.UserDAO;
import database.DatabaseConnection;
import model.Server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.InetAddress;
import java.sql.Connection;

public class LoginView extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    private JCheckBox showPasswordCheckbox;
    private ServerDAO serverDAO;  // Object to query the database for server details
    private DatabaseConnection dbConnection;  // Database connection object

    // Constructor accepts a properly initialized ServerDAO object
    public LoginView(ServerDAO serverDAO) {
        if (serverDAO == null) {
            throw new IllegalArgumentException("ServerDAO cannot be null");  // Early validation
        }
        this.serverDAO = serverDAO;  // Initialize the ServerDAO

        // JFrame setup
        setTitle("Mail Client - Login");
        setSize(450, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.add(new JLabel("<html><h2>Mail Client</h2><p>Secure Login</p></html>"));
        getContentPane().add(headerPanel, BorderLayout.NORTH);

        // Main panel (email and password fields)
        JPanel mainPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        mainPanel.add(new JLabel("Email:"));
        emailField = new JTextField(15);
        mainPanel.add(emailField);

        mainPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField(15);
        mainPanel.add(passwordField);

        // Panel for "Show Password" checkbox
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        showPasswordCheckbox = new JCheckBox("Show Password");
        showPasswordCheckbox.addActionListener(e -> togglePasswordVisibility());
        checkboxPanel.add(showPasswordCheckbox);
        mainPanel.add(checkboxPanel);

        getContentPane().add(mainPanel, BorderLayout.CENTER);

        // Buttons for login and register
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        mainPanel.add(buttonsPanel);
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        buttonsPanel.add(loginButton);
        buttonsPanel.add(registerButton);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> openRegisterView());

        // Status label (for showing login messages)
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        getContentPane().add(statusLabel, BorderLayout.PAGE_END);

        setLocationRelativeTo(null); // Center the window
        setVisible(true);

        // Initialize DatabaseConnection
        dbConnection = new DatabaseConnection();
    }

    // Toggle password visibility in the password field
    private void togglePasswordVisibility() {
        passwordField.setEchoChar(showPasswordCheckbox.isSelected() ? (char) 0 : '*');
    }

    // Login method triggered by login button
    private void login() {
        statusLabel.setText("Logging in...");
        String email = emailField.getText();

        // Validate email format
        if (!isValidEmail(email)) {
            statusLabel.setText("Invalid email format");
            return;
        }

        // Create a SwingWorker to handle login asynchronously
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    String password = new String(passwordField.getPassword());

                    // Fetch server IP and port from the database using ServerDAO
                    Server server = serverDAO.getServerIpAndPort();

                    if (server == null) {
                        statusLabel.setText("Server IP or Port not found in the databaseee");
                        return null;
                    }

                    String serverIp = server.getServerIp();
                    int serverPort = server.getServerPort();

                    // Create a MailClient object with the server details
                    MailClient tempClient = new MailClient(serverIp, serverPort);
                    String response = tempClient.sendRequest("LOGIN:" + email + ":" + password);

                    if (response.contains("successful")) {
                        // Save the client's IP address to the server (if necessary)
                        tempClient.sendRequest("SAVE_IP:" + email + ":" + InetAddress.getLocalHost().getHostAddress());

                        // Get the UserDAO object with a valid database connection
                        Connection connection = dbConnection.getConnection();
                        UserDAO userDAO = new UserDAO(connection);

                        // Open the MailClientView with the necessary objects
                        MailClient client = new MailClient(serverIp, serverPort);
                        new MailClientView(client, email, userDAO, serverDAO); // Pass UserDAO to MailClientView
                        dispose(); // Close the login window
                    } else {
                        statusLabel.setText(response); // Display login failure message
                    }
                } catch (Exception e) {
                    statusLabel.setText("An error occurred: " + e.getMessage());
                    e.printStackTrace();  // Print the error details
                }
                return null;
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);  // Re-enable the login button after the task is done
            }
        };

        loginButton.setEnabled(false);  // Disable the login button while the task is running
        worker.execute();  // Execute the login task asynchronously
    }

    // Validate email format using regex
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    // Open the RegisterView window
    private void openRegisterView() {
        new RegisterView(serverDAO);
        dispose();  // Close the LoginView
    }

}
