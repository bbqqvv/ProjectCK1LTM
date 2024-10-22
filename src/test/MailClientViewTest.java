package test;

import client.MailClient;
import client.MailClientView;

import javax.swing.*;

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
            MailClientView view = new MailClientView(client, "testUser"); // Sử dụng một tên người dùng giả
            view.setVisible(true);
        });
    }
}
