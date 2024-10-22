package client;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;

public class LoginView extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginView() {
        setTitle("Login");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2));

        add(new JLabel("Username:"));
        usernameField = new JTextField(15);
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField(15);
        add(passwordField);

        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        add(loginButton);
        add(registerButton);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> openRegisterView());

        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }

    private void login() {
        try {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            // Lấy địa chỉ IP của máy client
            String ipAddress = InetAddress.getLocalHost().getHostAddress();

            // Tạo đối tượng MailClient tạm thời để gửi yêu cầu đăng nhập
            MailClient tempClient = new MailClient("localhost", 4445);
            String response = tempClient.sendRequest("LOGIN:" + username + ":" + password);

            JOptionPane.showMessageDialog(this, response);
            if (response.contains("successful")) {
                // Lưu địa chỉ IP của client vào server
                tempClient.sendRequest("SAVE_IP:" + username + ":" + ipAddress);  // Lưu địa chỉ IP của client

                // Khởi tạo kết nối đến server với địa chỉ IP động
                MailClient client = new MailClient("localhost", 4445); // Thay bằng IP của server nếu cần
                new MailClientView(client, username);
                dispose();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void openRegisterView() {
        new RegisterView();
        dispose();
    }
}
