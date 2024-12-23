package test;

import client.MailClient;
import client.MailClientView;

import javax.swing.*;
import java.sql.SQLException;

public class MailClientViewTest {
    public static void main(String[] args) {
        // Khởi tạo MailClient
        MailClient client;
        try {
            client = new MailClient("localhost", 4445); // Địa chỉ server và port
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Tạo JFrame để kiểm tra giao diện
        SwingUtilities.invokeLater(() -> {
            MailClientView view = null; // Sử dụng một tên người dùng giả
            try {
                view = new MailClientView(client, "testUser", null, null);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            view.setVisible(true);
        });
    }
}
