package client;

import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private MailClient client;

    public LoginView(MailClient client) {
        this.client = client;
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
        registerButton.addActionListener(e -> register());

        setVisible(true);
    }

    private void login() {
        try {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String response = client.sendRequest("LOGIN:" + username + ":" + password);
            JOptionPane.showMessageDialog(this, response);
            if (response.contains("successful")) {
                new MailClientView(client, username);
                dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void register() {
        try {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String response = client.sendRequest("REGISTER:" + username + ":" + password);
            JOptionPane.showMessageDialog(this, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
