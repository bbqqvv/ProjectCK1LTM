package server;

import database.DatabaseConnection;
import dao.MailDAO;
import dao.UserDAO;
import java.sql.Connection;
import java.sql.SQLException;

public class ServerMain {
    public static void main(String[] args) {
        try {
            Connection connection = DatabaseConnection.getConnection(); // Lấy kết nối
            UserDAO userDAO = new UserDAO(connection); // Khởi tạo UserDAO
            MailDAO mailDAO = new MailDAO(connection); // Khởi tạo MailDAO
            MailServer mailServer = new MailServer(userDAO, mailDAO); // Khởi tạo MailServer với DAO
            
            // Tạo đối tượng ServerView và gán cho MailServer
            ServerView serverView = new ServerView();
            mailServer.setView(serverView); // Gán ServerView cho MailServer
            
            mailServer.start(); // Bắt đầu máy chủ
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Could not establish a connection to the database.");
        }
    }
}
