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
    private final ServerDAO serverDAO;
    private final DatabaseConnection dbConnection;

    public LoginView(ServerDAO serverDAO) {
        if (serverDAO == null) {
            throw new IllegalArgumentException("ServerDAO cannot be null");
        }
        this.serverDAO = serverDAO;
        this.dbConnection = new DatabaseConnection();

        initializeUI();
        setVisible(true);
    }

    /**
     * Initialize the UI components and layout.
     */
    private void initializeUI() {
        setTitle("Mail Client - Login");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // Add panels
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);

        // Apply global styles
        applyGlobalStyles();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(0, 123, 255)); // Blue background
        JLabel titleLabel = new JLabel("<html><h2 style='color: white;'>Mail Client</h2><p style='color: white;'>Secure Login</p></html>");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel);
        return headerPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        mainPanel.setBackground(Color.WHITE);

        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        showPasswordCheckbox = new JCheckBox("Show Password");
        showPasswordCheckbox.addActionListener(e -> togglePasswordVisibility());
        showPasswordCheckbox.setBackground(Color.WHITE);

        mainPanel.add(new JLabel("Email:"));
        mainPanel.add(emailField);
        mainPanel.add(new JLabel("Password:"));
        mainPanel.add(passwordField);
        mainPanel.add(new JLabel());
        mainPanel.add(showPasswordCheckbox);

        return mainPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        loginButton = createStyledButton("Login", new Color(40, 167, 69)); // Green
        registerButton = createStyledButton("Register", new Color(220, 53, 69)); // Red

        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> openRegisterView());

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        footerPanel.add(buttonPanel, BorderLayout.NORTH);

        // Status Label
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        footerPanel.add(statusLabel, BorderLayout.SOUTH);

        return footerPanel;
    }

    private void togglePasswordVisibility() {
        passwordField.setEchoChar(showPasswordCheckbox.isSelected() ? (char) 0 : '*');
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (!isValidEmail(email)) {
            showStatusMessage("Invalid email format.");
            return;
        }

        if (password.isEmpty()) {
            showStatusMessage("Password cannot be empty.");
            return;
        }

        performLoginAsync(email, password);
    }

    private void performLoginAsync(String email, String password) {
        statusLabel.setText("Logging in...");
        loginButton.setEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    Server server = serverDAO.getServerIpAndPort();
                    if (server == null) {
                        throw new IllegalStateException("Server details not found.");
                    }

                    MailClient mailClient = new MailClient(server.getServerIp(), server.getServerPort());
                    String response = mailClient.sendRequest("LOGIN:" + email + ":" + password);

                    if (response.contains("successful")) {
                        String localIp = InetAddress.getLocalHost().getHostAddress();
                        mailClient.sendRequest("SAVE_IP:" + email + ":" + localIp);

                        Connection connection = dbConnection.getConnection();
                        UserDAO userDAO = new UserDAO(connection);

                        SwingUtilities.invokeLater(() -> openMailClientView(mailClient, email, userDAO));
                    } else {
                        showStatusMessage(response);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showStatusMessage("Error: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
            }
        };
        worker.execute();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    private void showStatusMessage(String message) {
        statusLabel.setText(message);
    }

    private void openRegisterView() {
        new RegisterView(serverDAO);
        dispose();
    }

    private void openMailClientView(MailClient mailClient, String email, UserDAO userDAO) {
        new MailClientView(mailClient, email, userDAO, serverDAO);
        dispose();
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        return button;
    }

    private void applyGlobalStyles() {
        UIManager.put("Label.font", new Font("Arial", Font.PLAIN, 14));
        UIManager.put("TextField.font", new Font("Arial", Font.PLAIN, 14));
        UIManager.put("PasswordField.font", new Font("Arial", Font.PLAIN, 14));
        UIManager.put("CheckBox.font", new Font("Arial", Font.PLAIN, 14));
    }
}
