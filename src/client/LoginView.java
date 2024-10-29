package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.InetAddress;

public class LoginView extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    private JCheckBox showPasswordCheckbox;

    public LoginView() {
        setTitle("Mail Client - Login");
        setSize(450, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        // Panel tiêu đề
        JPanel headerPanel = new JPanel();
        headerPanel.add(new JLabel("<html><h2>Mail Client</h2><p>Secure Login</p></html>"));
        getContentPane().add(headerPanel, BorderLayout.NORTH);

        // Panel chính
        JPanel mainPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        mainPanel.add(new JLabel("Email:"));
        emailField = new JTextField(15);
        mainPanel.add(emailField);

        mainPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField(15);
        mainPanel.add(passwordField);

        // Tạo panel cho "Show Password" checkbox
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        showPasswordCheckbox = new JCheckBox("Show Password");
        showPasswordCheckbox.addActionListener(e -> togglePasswordVisibility());
        checkboxPanel.add(showPasswordCheckbox);
        mainPanel.add(checkboxPanel);

        getContentPane().add(mainPanel, BorderLayout.CENTER);

        // Panel nút đăng nhập và đăng ký
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        mainPanel.add(buttonsPanel);
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        buttonsPanel.add(loginButton);
        buttonsPanel.add(registerButton);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> openRegisterView());

        // Label trạng thái
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        getContentPane().add(statusLabel, BorderLayout.PAGE_END);

        setLocationRelativeTo(null); // Căn giữa cửa sổ
        setVisible(true);
    }

    private void togglePasswordVisibility() {
        passwordField.setEchoChar(showPasswordCheckbox.isSelected() ? (char) 0 : '*');
    }

    private void login() {
        statusLabel.setText("Logging in...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    String email = emailField.getText();
                    String password = new String(passwordField.getPassword());

                    // Lấy địa chỉ IP của máy
                    String ipAddress = InetAddress.getLocalHost().getHostAddress();

                    // Tạo đối tượng MailClient với địa chỉ IP của máy
                    MailClient tempClient = new MailClient(ipAddress, 4445); // Sử dụng ipAddress thay vì "localhost"
                    String response = tempClient.sendRequest("LOGIN:" + email + ":" + password);

                    if (response.contains("successful")) {
                        // Lưu địa chỉ IP vào máy chủ
                        tempClient.sendRequest("SAVE_IP:" + email + ":" + ipAddress);

                        // Mở giao diện MailClientView
                        MailClient client = new MailClient(ipAddress, 4445); // Sử dụng ipAddress thay vì "localhost"
                        new MailClientView(client, email);
                        dispose();
                    } else {
                        statusLabel.setText(response);
                    }
                } catch (Exception e) {
                    statusLabel.setText("An error occurred: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
            }
        };

        loginButton.setEnabled(false);
        worker.execute();
    }


    private void openRegisterView() {
        new RegisterView();
        dispose();
    }
}
