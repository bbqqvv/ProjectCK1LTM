package server;

import model.User;
import dao.MailDAO;
import dao.UserDAO;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MailServer {
    private static final int PORT = 4445;
    private DatagramSocket socket;
    private ServerView view;
    private UserDAO userDAO;
    private MailDAO mailDAO;

    public MailServer(UserDAO userDAO, MailDAO mailDAO) {
        this.userDAO = userDAO;
        this.mailDAO = mailDAO;
    }

    public void setView(ServerView view) {
        this.view = view;
    }

    public void start() {
        try {
            socket = new DatagramSocket(PORT);
            view.appendLog("Mail server is running on port " + PORT);

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String request = new String(packet.getData(), 0, packet.getLength()).trim();
                view.appendLog("Received: " + request);
                handleRequest(request, packet);
            }
        } catch (IOException e) {
            view.appendLog("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleRequest(String request, DatagramPacket packet) throws IOException {
        List<String> tokens = new ArrayList<>(Arrays.asList(request.split(":")));
        if (tokens.isEmpty()) {
            sendResponse("Invalid command", packet);
            return;
        }

        String command = tokens.remove(0).toUpperCase(); // Lấy lệnh và loại bỏ nó khỏi danh sách

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

            default:
                sendResponse("Invalid command", packet);
                break;
        }
    }

    private void handleRegister(List<String> tokens, DatagramPacket packet) throws IOException {
        if (tokens.size() < 3) {
            sendResponse("Invalid registration request", packet);
            return;
        }
        String username = tokens.remove(0);
        String email = tokens.remove(0);
        String password = tokens.remove(0);
        
        // Chỉ khởi tạo id, ipAddress, createdAt và updatedAt khi cần
        User newUser = new User(0, username, password, email); // Khởi tạo User không cần địa chỉ IP
        boolean registrationSuccess = userDAO.addUser(newUser);
        sendResponse(registrationSuccess ? "Register successful" : "Register failed", packet);
    }

    private void handleLogin(List<String> tokens, DatagramPacket packet) throws IOException {
        if (tokens.size() < 2) {
            sendResponse("Invalid login request", packet);
            return;
        }
        String username = tokens.remove(0);
        String password = tokens.remove(0);
        User loginUser = new User(0, username, password, null); // Khởi tạo User không cần email
        boolean loginSuccess = userDAO.loginUser(loginUser);
        
        if (loginSuccess) {
            String ipAddress = packet.getAddress().getHostAddress(); // Lấy địa chỉ IP từ DatagramPacket
            userDAO.updateUserIpAddress(username, ipAddress); // Cập nhật địa chỉ IP sau khi đăng nhập thành công
        }
        
        sendResponse(loginSuccess ? "Login successful" : "Login failed", packet);
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
        String username = tokens.remove(0);
        String emails = mailDAO.getAllMailsForUser(username);
        sendResponse(emails.isEmpty() ? "No emails found" : emails, packet);
    }

    private void sendResponse(String response, DatagramPacket packet) throws IOException {
        byte[] buf = response.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
        socket.send(responsePacket);
        view.appendLog("Sent response: " + response);
    }
}
