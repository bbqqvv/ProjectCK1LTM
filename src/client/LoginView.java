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
import java.sql.SQLException;
import java.util.regex.Pattern;

public class LoginView extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    private JCheckBox showPasswordCheckbox;
    private final ServerDAO serverDAO;
    private final MailClient mailClient;

    public LoginView(ServerDAO serverDAO, MailClient mailClient) {
        if (serverDAO == null) {
            throw new IllegalArgumentException("ServerDAO cannot be null");
        }
        if (mailClient == null) {
            throw new IllegalArgumentException("MailClient cannot be null");
        }

        this.serverDAO = serverDAO;
        this.mailClient = mailClient;

        // JFrame setup
        setTitle("Mail Client - Login");
        setSize(450, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel headerLabel = new JLabel("<html><h2>Mail Client</h2><p>Secure Login</p></html>");
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

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

        add(mainPanel, BorderLayout.CENTER);

        // Buttons for login and register
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Added spacing for buttons
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        buttonsPanel.add(loginButton);
        buttonsPanel.add(registerButton);
        add(buttonsPanel, BorderLayout.NORTH);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> openRegisterView());

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(statusLabel, BorderLayout.PAGE_END);

        setVisible(true);

        // Add KeyListener to trigger login on Enter press
        emailField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    login(); // Trigger login when Enter is pressed
                }
            }
        });

        passwordField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    login(); // Trigger login when Enter is pressed
                }
            }
        });
    }

    private void togglePasswordVisibility() {
        passwordField.setEchoChar(showPasswordCheckbox.isSelected() ? (char) 0 : '*');
    }

    private void login() {
        statusLabel.setText("Logging in...");
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Email and password are required.");
            return;
        }

        if (!isValidEmail(email)) {
            statusLabel.setText("Invalid email format.");
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    // Set cứng IP và cổng
                    String fixedServerIp = "192.168.1.3"; // Địa chỉ IP cố định
                    int fixedUdpPort = 4445;              // Cổng cố định

                    MailClient mailClient = new MailClient(fixedServerIp, fixedUdpPort);
                    String response = mailClient.sendRequest("LOGIN:" + email + ":" + password);

                    if (response.contains("successful")) {
                        String localIp = InetAddress.getLocalHost().getHostAddress();
                        mailClient.sendRequest("SAVE_IP:" + email + ":" + localIp);

                        Connection connection = DatabaseConnection.getConnection();
                        UserDAO userDAO = new UserDAO(connection);

                        SwingUtilities.invokeLater(() -> {
                            try {
                                openMailClientView(mailClient, email, userDAO);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } else {
//                        showStatusMessage(response);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
//                    showStatusMessage("Error: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true); // Kích hoạt lại nút Login sau khi xử lý
            }
        };

        loginButton.setEnabled(false); // Vô hiệu hóa nút Login trong khi xử lý
        worker.execute();
    }


    private void openMailClientView(MailClient mailClient, String email, UserDAO userDAO) throws SQLException {
        new MailClientView(mailClient, email, userDAO, serverDAO);
        dispose();
    }
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    private void openRegisterView() {
        new RegisterView(serverDAO, mailClient);  // Truyền MailClient cho RegisterView
        dispose();  // Đóng LoginView
    }
}
