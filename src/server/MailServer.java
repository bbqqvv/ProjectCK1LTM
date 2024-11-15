package server;

import model.Server;
import model.User;
import dao.MailDAO;
import dao.ServerDAO;
import dao.UserDAO;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MailServer {
    private static final int PORT = 4445;
    private DatagramSocket socket;
    private ServerView view;
    private UserDAO userDAO;
    private MailDAO mailDAO;
    private ServerDAO serverDAO; // Thêm trường ServerDAO
    private ExecutorService executor = Executors.newFixedThreadPool(10);  // Tạo một ExecutorService với 10 luồng

    public MailServer(UserDAO userDAO, MailDAO mailDAO, ServerDAO serverDAO) {
        this.userDAO = userDAO;
        this.mailDAO = mailDAO;
        this.serverDAO = serverDAO;
    }

    public void setView(ServerView view) {
        this.view = view;
    }


    public void start() {
        try {
            socket = new DatagramSocket(PORT);
            view.appendLog("Mail server is running on port " + PORT);

            // Lưu địa chỉ IP và port của server vào CSDL
            String serverIp = InetAddress.getLocalHost().getHostAddress();
            serverDAO.saveServer(serverIp, PORT); // Lưu IP và port

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String request = new String(packet.getData(), 0, packet.getLength()).trim();
                view.appendLog("Received: " + request);

                // Xử lý yêu cầu trong một thread từ ExecutorService
                executor.submit(() -> {
                    try {
                        handleRequest(request, packet);
                    } catch (IOException e) {
                        view.appendLog("Error processing request: " + e.getMessage());
                    }
                });
            }
        } catch (IOException e) {
            view.appendLog("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void stop() throws UnknownHostException {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                view.appendLog("Mail server stopped.");
            }

            // Xóa địa chỉ IP và port của server khỏi CSDL
            String serverIp = InetAddress.getLocalHost().getHostAddress();
            boolean isDeleted = serverDAO.deleteServer(serverIp, PORT);
            
            if (isDeleted) {
                view.appendLog("Server information removed from database.");
            } else {
                view.appendLog("Failed to remove server information from database.");
            }
            
        } catch (IOException e) {
            view.appendLog("Error while stopping server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Đảm bảo trạng thái của server được cập nhật và không bị treo
            // Update the view to indicate that the server has stopped
            view.appendLog("Server is no longer running.");
        }
    }

    private void handleRequest(String request, DatagramPacket packet) throws IOException {
        List<String> tokens = new ArrayList<>(Arrays.asList(request.split(":")));
        if (tokens.isEmpty()) {
            sendResponse("Invalid command", packet);
            return;
        }

        String command = tokens.remove(0).toUpperCase();

        switch (command) {
            case "REGISTER":
                handleRegister(tokens, packet);
                break;

            case "LOGIN":
                handleLogin(tokens, packet);
                break;

            case "SEND_EMAIL":
                handleSendEmail(tokens, packet);
                break;

            case "LOAD_EMAILS":
                handleLoadEmails(tokens, packet);
                break;

            case "SEARCH_EMAILS":
                handleSearchEmails(tokens, packet);
                break;

            case "DELETE_EMAIL":  // Xử lý xóa email
                handleDeleteEmail(tokens, packet);
                break;

            default:
                sendResponse("Invalid command", packet);
                break;
        }
    }
    
    private void handleDeleteEmail(List<String> tokens, DatagramPacket packet) throws IOException {
        if (tokens.size() < 2) {
            sendResponse("Invalid delete email request", packet);
            return;
        }

        try {
            String email = tokens.remove(0); // Lấy email người dùng (exam@gmail.com)
            int mailId = Integer.parseInt(tokens.remove(0)); // Lấy ID của email cần xóa (7)

            System.out.println("Attempting to delete email with ID: " + mailId + " for user: " + email);  // Ghi log ID email và người dùng

            // Kiểm tra email có tồn tại trong CSDL trước khi xóa
            boolean exists = mailDAO.mailExists(mailId);
            if (!exists) {
                sendResponse("Email with ID " + mailId + " does not exist", packet);
                return;
            }

            // Tiến hành gọi phương thức xóa
            boolean isDeleted = mailDAO.deleteMail(mailId);

            // Ghi log kết quả xóa
            if (isDeleted) {
                System.out.println("Email with ID " + mailId + " deleted successfully.");
                sendResponse("Email deleted successfully", packet);
            } else {
                System.out.println("Failed to delete email with ID " + mailId);
                sendResponse("Failed to delete email", packet);
            }
        } catch (NumberFormatException e) {
            sendResponse("Invalid email ID format", packet);
        }
    }


    
    private void handleSearchEmails(List<String> tokens, DatagramPacket packet) throws IOException {
        if (tokens.size() < 3) {
            sendResponse("Invalid search request", packet);
            return;
        }
        String email = tokens.remove(0);   // Email của người dùng cần tìm kiếm
        String keyword = tokens.remove(0); // Từ khóa tìm kiếm
        int currentPage = Integer.parseInt(tokens.remove(0)); // Trang hiện tại
        int emailsPerPage = Integer.parseInt(tokens.remove(0)); // Số email trên mỗi trang

        // Tìm kiếm email trong cơ sở dữ liệu
        String foundEmails = mailDAO.searchMailsForUser(email, keyword, currentPage, emailsPerPage);
        sendResponse(foundEmails.isEmpty() ? "No emails found" : foundEmails, packet);
    }

    private void handleRegister(List<String> tokens, DatagramPacket packet) throws IOException {
        if (tokens.size() < 3) {
            sendResponse("Invalid registration request", packet);
            return;
        }
        String username = tokens.remove(0);
        String email = tokens.remove(0);
        String password = tokens.remove(0);
        
        User newUser = new User(0, username, password, email);
        boolean registrationSuccess = userDAO.addUser(newUser);
        sendResponse(registrationSuccess ? "Register successful" : "Register failed", packet);
    }

    private void handleLogin(List<String> tokens, DatagramPacket packet) throws IOException {
        if (tokens.size() < 2) {
            sendResponse("Invalid login request", packet);
            return;
        }
        String email = tokens.remove(0);
        String password = tokens.remove(0);
        User loginUser = new User(0, null, password, email);
        boolean loginSuccess = userDAO.loginUser(loginUser);

        if (loginSuccess) {
            String ipAddress = packet.getAddress().getHostAddress();
            userDAO.updateUserIpAddress(email, ipAddress);
            
            // Lấy địa chỉ IP và Port của server từ database
            Server serverInfo = serverDAO.getServerIpAndPort();
            
            // Get username for the logged-in user
            String username = userDAO.getUsername(email);
            
            // Tạo thông báo phản hồi bao gồm IP và Port của server
            String response = "Login successful. Welcome, " + username + "!" 
                              + (serverInfo != null ? " Server IP: " + serverInfo.getServerIp() 
                              + ", Port: " + serverInfo.getServerPort() : " Server info not found.");
            sendResponse(response, packet);
        } else {
            sendResponse("Login failed", packet);
        }
    }

    private void handleSendEmail(List<String> tokens, DatagramPacket packet) throws IOException {
        if (tokens.size() < 4) {
            sendResponse("Invalid email request", packet);
            return;
        }
        String sender = tokens.remove(0);
        String receiver = tokens.remove(0);
        String subject = tokens.remove(0);
        String content = tokens.remove(0);
        boolean emailSent = mailDAO.addMail(new model.Mail(0, sender, receiver, subject, content, new java.util.Date(), false));
        sendResponse(emailSent ? "Email sent" : "Failed to send email", packet);
    }

    private void handleLoadEmails(List<String> tokens, DatagramPacket packet) throws IOException {
        if (tokens.size() < 1) {
            sendResponse("Invalid load emails request", packet);
            return;
        }
        String email = tokens.remove(0);
        String emails = mailDAO.getAllMailsForUser(email);
        sendResponse(emails.isEmpty() ? "No emails found" : emails, packet);
    }

    private void sendResponse(String response, DatagramPacket packet) throws IOException {
        byte[] buf = response.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
        socket.send(responsePacket);
        view.appendLog("Sent response: " + response);
    }
}