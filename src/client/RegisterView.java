package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

import dao.ServerDAO;

import java.awt.*;

public class RegisterView extends JFrame {
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton cancelButton;
	private JTextComponent statusLabel;
	private ServerDAO serverDAO;

    public RegisterView() {
        setTitle("Register");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        // Tạo panel tiêu đề
        JPanel headerPanel = new JPanel();
        headerPanel.add(new JLabel("<html><h2>Register Account</h2></html>"));
        getContentPane().add(headerPanel, BorderLayout.NORTH);

        // Tạo panel chính với lưới
        JPanel mainPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        mainPanel.add(new JLabel("Username:"));
        usernameField = new JTextField(15);
        mainPanel.add(usernameField);

        mainPanel.add(new JLabel("Email:"));
        emailField = new JTextField(15);
        mainPanel.add(emailField);

        mainPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField(15);
        mainPanel.add(passwordField);

        mainPanel.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField(15);
        mainPanel.add(confirmPasswordField);

        getContentPane().add(mainPanel, BorderLayout.CENTER);

        // Tạo panel chứa nút đăng ký và hủy
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        registerButton = new JButton("Register");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        registerButton.addActionListener(e -> register());
        cancelButton.addActionListener(e -> openLoginView());

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null); // Căn giữa cửa sổ
        setVisible(true);
    }

    private void register() {
        try {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            // Kiểm tra tính hợp lệ của thông tin đầu vào
            if (!password.equals(confirmPassword)) {
                statusLabel.setText("Passwords do not match.");
                return;
            }

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                statusLabel.setText("All fields are required.");
                return;
            }

            // Tạo đối tượng MailClient tạm thời để gửi yêu cầu đăng ký
            MailClient tempClient = new MailClient("localhost", 4445);
            String request = "REGISTER:" + username + ":" + email + ":" + password;
            System.out.println("Sending request: " + request);
            
            String response = tempClient.sendRequest(request);
            JOptionPane.showMessageDialog(this, response);
            if (response.contains("successful")) {
                new LoginView(serverDAO);
                dispose();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void openLoginView() {
   

        new LoginView(serverDAO);
        dispose();
    }
}