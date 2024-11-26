package server;

import dao.AttachmentDAO;
import database.DatabaseConnection;
import dao.MailDAO;
import dao.UserDAO;
import dao.ServerDAO;
import java.sql.Connection;
import java.sql.SQLException;

public class ServerMain {
    public static void main(String[] args) {
        try {
            Connection connection = DatabaseConnection.getConnection(); // Lấy kết nối
            UserDAO userDAO = new UserDAO(connection); // Khởi tạo UserDAO
            MailDAO mailDAO = new MailDAO(connection); // Khởi tạo MailDAO
            AttachmentDAO attachmentDAO = new AttachmentDAO(connection);

            ServerDAO serverDAO = new ServerDAO(connection); // Khởi tạo ServerDAO
            MailServer mailServer = new MailServer(userDAO, mailDAO, serverDAO,attachmentDAO);
            ServerView serverView = new ServerView(mailServer); // Pass mailServer to ServerView
            mailServer.setView(serverView); // Gán ServerView cho MailServer

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Could not establish a connection to the database.");
        }
    }
}
