package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import dao.ServerDAO;
import java.awt.*;
import java.util.regex.Pattern;

public class RegisterView extends JFrame {
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton cancelButton;
    private JLabel statusLabel; // Dùng JLabel để hiển thị trạng thái
    private final ServerDAO serverDAO;
    private final MailClient mailClient;

    public RegisterView(ServerDAO serverDAO, MailClient mailClient) {
        this.serverDAO = serverDAO;
        this.mailClient = mailClient;

        // Cài đặt cửa sổ chính
        setTitle("Register");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        // Tiêu đề
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel headerLabel = new JLabel("Register Account");
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Nội dung chính
        JPanel mainPanel = new JPanel(new GridLayout(5, 2, 15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordField = new JPasswordField(20);

        mainPanel.add(usernameLabel);
        mainPanel.add(usernameField);
        mainPanel.add(emailLabel);
        mainPanel.add(emailField);
        mainPanel.add(passwordLabel);
        mainPanel.add(passwordField);
        mainPanel.add(confirmPasswordLabel);
        mainPanel.add(confirmPasswordField);
        add(mainPanel, BorderLayout.CENTER);

        // Panel trạng thái
        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        add(statusLabel, BorderLayout.SOUTH);

        // Nút hành động
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        registerButton = new JButton("Register");
        cancelButton = new JButton("Cancel");
        registerButton.setPreferredSize(new Dimension(120, 35));
        cancelButton.setPreferredSize(new Dimension(120, 35));
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Hành động khi bấm nút
        registerButton.addActionListener(e -> register());
        cancelButton.addActionListener(e -> openLoginView());

        // Hiển thị cửa sổ ở giữa màn hình
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void register() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Kiểm tra các trường hợp lỗi nhập liệu
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            statusLabel.setText("All fields are required.");
            return;
        }

        if (!isValidEmail(email)) {
            statusLabel.setText("Invalid email format.");
            return;
        }

        if (password.length() < 6) {
            statusLabel.setText("Password must be at least 6 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Passwords do not match.");
            return;
        }

        // Gửi yêu cầu đăng ký tới server
        try {
            String request = "REGISTER:" + username + ":" + email + ":" + password;
            String response = mailClient.sendRequest("REGISTER", request, false,null); // Sử dụng UDP

            // Thông báo kết quả
            if (response.contains("successful")) {
                JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                openLoginView();
                dispose();
            } else {
                statusLabel.setText(response); // Hiển thị lỗi nếu đăng ký thất bại
            }
        } catch (Exception e) {
            statusLabel.setText("An error occurred. Please try again.");
            e.printStackTrace();
        }
    }

    private void openLoginView() {
        new LoginView(serverDAO, mailClient); // Chuyển sang màn hình đăng nhập
        dispose();
    }

    /**
     * Kiểm tra định dạng email hợp lệ.
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }
}
