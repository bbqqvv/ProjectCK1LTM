package client;

import java.net.InetAddress;

import javax.swing.SwingUtilities;

public class ClientMain {
    public static void main(String[] args) {
        try {
            SwingUtilities.invokeLater(() -> {

            // Khởi tạo đối tượng MailClient sau khi đăng nhập thành công
            new LoginView(); // Không cần cung cấp serverAddress ban đầu, sẽ lấy sau khi đăng nhập
            });
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
