package client;

import javax.swing.*;
import java.awt.*;

public class RegisterView extends JFrame {
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;

    public RegisterView() {
        setTitle("Register");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(5, 2));

        add(new JLabel("Username:"));
        usernameField = new JTextField(15);
        add(usernameField);

        add(new JLabel("Email:"));
        emailField = new JTextField(15);
        add(emailField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField(15);
        add(passwordField);

        add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField(15);
        add(confirmPasswordField);

        registerButton = new JButton("Register");
        add(new JLabel());  // Empty label for spacing
        add(registerButton);

        registerButton.addActionListener(e -> register());

        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }

    private void register() {
        try {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Tạo đối tượng MailClient tạm thời để gửi yêu cầu đăng ký
            MailClient tempClient = new MailClient("localhost", 4445);
            String request = "REGISTER:" + username + ":" + email + ":" + password;
            System.out.println("Sending request: " + request);
            
            String response = tempClient.sendRequest(request);
            JOptionPane.showMessageDialog(this, response);
            if (response.contains("successful")) {
                new LoginView();
                dispose();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

}
