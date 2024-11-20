//package service;
//
//import dao.ServerDAO;
//import dao.UserDAO;
//import database.DatabaseConnection;
//import model.Server;
//import java.net.InetAddress;
//import java.sql.Connection;
//
//import client.MailClient;
//
//public class LoginService {
//
//    private ServerDAO serverDAO;
//    private DatabaseConnection dbConnection;
//
//    public LoginService(ServerDAO serverDAO, DatabaseConnection dbConnection) {
//        this.serverDAO = serverDAO;
//        this.dbConnection = dbConnection;
//    }
//
//    // Authenticate user login
//    public String authenticateUser(String email, String password) {
//        try {
//            // Fetch server IP and port from the database using ServerDAO
//            Server server = serverDAO.getServerIpAndPort();
//
//            if (server == null) {
//                return "Server IP or Port not found in the database";
//            }
//
//            String serverIp = server.getServerIp();
//            int serverPort = server.getServerPort();
//
//            // Create a MailClient object with the server details
//            MailClient tempClient = new MailClient(serverIp, serverPort);
//            String response = tempClient.sendRequest("LOGIN:" + email + ":" + password);
//
//            if (response.contains("successful")) {
//                // Save the client's IP address to the server (if necessary)
//                tempClient.sendRequest("SAVE_IP:" + email + ":" + InetAddress.getLocalHost().getHostAddress());
//
//                // Get the UserDAO object with a valid database connection
//                Connection connection = dbConnection.getConnection();
//                UserDAO userDAO = new UserDAO(connection);
//
//                // Return success response with user details for further handling
//                return "successful";
//            } else {
//                return response; // Login failed message
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "An error occurred: " + e.getMessage();
//        }
//    }
//}
