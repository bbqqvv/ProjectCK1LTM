package server;

import dao.AttachmentDAO;
import model.Attachment;
import model.Mail;
import model.Server;
import model.User;
import dao.MailDAO;
import dao.ServerDAO;
import dao.UserDAO;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MailServer {
    private static final int UDP_PORT = 4445;
    private DatagramSocket socket;
    private static final int TCP_PORT = 5555; // Cổng TCP
    private ServerSocket tcpServerSocket;
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
            // Khởi tạo UDP server
            socket = new DatagramSocket(UDP_PORT);
            view.appendLog("Mail server is running on UDP port " + UDP_PORT);

            // Lưu địa chỉ IP và port của server vào CSDL
            String serverIp = InetAddress.getLocalHost().getHostAddress();
            serverDAO.saveServer(serverIp, UDP_PORT); // Lưu cả UDP và TCP port vào CSDL

            // Chạy UDP server trong một luồng riêng biệt
            new Thread(this::startUDPServer).start();

        } catch (IOException e) {
            view.appendLog("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startUDPServer() {
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String request = new String(packet.getData(), 0, packet.getLength()).trim();
                view.appendLog("Received UDP request: " + request);

                // Xử lý yêu cầu trong một thread từ ExecutorService
                executor.submit(() -> {
                    try {
                        handleRequest(request, packet);
                    } catch (IOException e) {
                        view.appendLog("Error processing UDP request: " + e.getMessage());
                    }
                });
            }
        } catch (IOException e) {
            view.appendLog("Error in UDP server: " + e.getMessage());
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
            boolean isDeleted = serverDAO.deleteServer(serverIp, UDP_PORT);

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


    private void handleTCPRequest(Socket clientSocket) {
        try (InputStream input = clientSocket.getInputStream();
             OutputStream output = clientSocket.getOutputStream();
             DataInputStream dataInputStream = new DataInputStream(input);
             DataOutputStream dataOutputStream = new DataOutputStream(output)) {

            String command = dataInputStream.readUTF(); // Đọc lệnh từ client
            System.out.println("Received TCP command: " + command);

            switch (command) {
                case "SEND_EMAIL_ATTACHMENT":
                    // Xử lý tải tệp đính kèm từ client
                    handleFileUpload(dataInputStream, dataOutputStream);
                    break;

                default:
                    // Lệnh không hợp lệ
                    System.err.println("Unknown TCP command: " + command);
                    dataOutputStream.writeUTF("Unknown command: " + command);
                    break;
            }

        } catch (IOException e) {
            // Log lỗi nếu xảy ra trong quá trình xử lý
            String errorMessage = "Error in handleTCPRequest: " + e.getMessage();
            view.appendLog(errorMessage);
            e.printStackTrace();

            // Gửi phản hồi lỗi về client
            try (OutputStream output = clientSocket.getOutputStream();
                 DataOutputStream dataOutputStream = new DataOutputStream(output)) {
                dataOutputStream.writeUTF("Server error: " + e.getMessage());
            } catch (IOException ex) {
                System.err.println("Error sending error response to client: " + ex.getMessage());
            }

        } finally {
            // Đảm bảo đóng socket
            try {
                clientSocket.close();
                System.out.println("Socket closed for client: " + clientSocket.getInetAddress().getHostAddress());
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }


    private void handleFileUpload(DataInputStream dis, DataOutputStream dos) throws IOException {
        int fileCount = dis.readInt(); // Nhận số lượng tệp
        for (int i = 0; i < fileCount; i++) {
            String fileName = dis.readUTF(); // Nhận tên tệp
            long fileSize = dis.readLong(); // Nhận kích thước tệp

            File attachmentsDir = new File("attachments");
            if (!attachmentsDir.exists() && !attachmentsDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + attachmentsDir.getAbsolutePath());
            }

            File file = new File(attachmentsDir, fileName.replaceAll("[\\\\/:*?\"<>|]", "_"));

            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                long remaining = fileSize;
                int bytesRead;

                while (remaining > 0 && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    remaining -= bytesRead;
                }
            }

            // Gửi phản hồi rằng tệp đã được nhận
            dos.writeUTF("FILE_RECEIVED");
        }

        // Gửi phản hồi cuối cùng
        dos.writeUTF("ALL_FILES_RECEIVED");
    }


    private void handleRequest(String request, DatagramPacket packet) throws IOException {
        List<String> tokens = new ArrayList<>(Arrays.asList(request.split(":")));
        if (tokens.isEmpty()) {
            sendResponseUDP("Invalid command", packet);
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

            case "REPLY_EMAIL":
                handleReplyEmail(tokens, packet);

            case "CHAT":
                handleChat(tokens, packet);
            default:
                sendResponseUDP("Invalid command", packet);
                break;
        }
    }

    private void handleChat(List<String> tokens, DatagramPacket packet) {
        // TODO Auto-generated method stub

    }

    private void handleReplyEmail(List<String> tokens, DatagramPacket packet) {
        // TODO Auto-generated method stub

    }

    private void handleDeleteEmail(List<String> tokens, DatagramPacket packet) throws IOException {
        if (tokens.size() < 2) {
            sendResponseUDP("Invalid delete email request", packet);
            return;
        }

        try {
            String email = tokens.remove(0); // Lấy email người dùng (exam@gmail.com)
            int mailId = Integer.parseInt(tokens.remove(0)); // Lấy ID của email cần xóa (7)

            System.out.println("Attempting to delete email with ID: " + mailId + " for user: " + email);  // Ghi log ID email và người dùng

            // Kiểm tra email có tồn tại trong CSDL trước khi xóa
            boolean exists = mailDAO.mailExists(mailId);
            if (!exists) {
                sendResponseUDP("Email with ID " + mailId + " does not exist", packet);
                return;
            }

            // Tiến hành gọi phương thức xóa
            boolean isDeleted = mailDAO.deleteMail(mailId);

            // Ghi log kết quả xóa
            if (isDeleted) {
                System.out.println("Email with ID " + mailId + " deleted successfully.");
                sendResponseUDP("Email deleted successfully", packet);
            } else {
                System.out.println("Failed to delete email with ID " + mailId);
                sendResponseUDP("Failed to delete email", packet);
            }
        } catch (NumberFormatException e) {
            sendResponseUDP("Invalid email ID format", packet);
        }
    }


    private void handleSearchEmails(List<String> tokens, DatagramPacket packet) throws IOException {
        if (tokens.size() < 3) {
            sendResponseUDP("Invalid search request", packet);
            return;
        }
        String email = tokens.remove(0);   // Email của người dùng cần tìm kiếm
        String keyword = tokens.remove(0); // Từ khóa tìm kiếm
        int currentPage = Integer.parseInt(tokens.remove(0)); // Trang hiện tại
        int emailsPerPage = Integer.parseInt(tokens.remove(0)); // Số email trên mỗi trang

        // Tìm kiếm email trong cơ sở dữ liệu
        String foundEmails = mailDAO.searchMailsForUser(email, keyword, currentPage, emailsPerPage);
        sendResponseUDP(foundEmails.isEmpty() ? "No emails found" : foundEmails, packet);
    }

    private void handleRegister(List<String> tokens, DatagramPacket packet) throws IOException {
        if (tokens.size() < 3) {
            sendResponseUDP("Invalid registration request", packet);
            return;
        }
        String username = tokens.remove(0);
        String email = tokens.remove(0);
        String password = tokens.remove(0);

        User newUser = new User(0, username, password, email, false);
        boolean registrationSuccess = userDAO.addUser(newUser);
        sendResponseUDP(registrationSuccess ? "Register successful" : "Register failed", packet);
    }


    private void handleLogin(List<String> tokens, DatagramPacket packet) throws IOException {
        if (tokens.size() < 2) {
            sendResponseUDP("Invalid login request", packet);
            return;
        }
        String email = tokens.remove(0);
        String password = tokens.remove(0);

        // Kiểm tra xem người dùng đã đăng nhập hay chưa
        if (userDAO.isUserLoggedIn(email)) {
            sendResponseUDP("You are already logged in.", packet);
            return;
        }
        // Tạo đối tượng User với trạng thái isLogin mặc định là false
        User loginUser = new User(0, null, password, email, false);  // isLogin mặc định là false
        boolean loginSuccess = userDAO.loginUser(loginUser);

        if (loginSuccess) {
            // Cập nhật trạng thái đăng nhập của người dùng
            userDAO.updateLoginStatus(email, true);  // Cập nhật isLogin = true

            // Cập nhật địa chỉ IP của người dùng
            String ipAddress = packet.getAddress().getHostAddress();
            userDAO.updateUserIpAddress(email, ipAddress);

            // Lấy thông tin máy chủ từ database (cả UDP và TCP port)
            Server serverInfo = serverDAO.getServerIpAndPort(); // Đây là phương thức lấy thông tin máy chủ (UDP + TCP)

            // Lấy tên người dùng
            String username = userDAO.getUsername(email);

            // Kiểm tra và xử lý thông tin server
            if (serverInfo != null) {
                // Gửi phản hồi với thông tin máy chủ và cổng TCP và UDP
                String response = "Login successful. Welcome, " + username + "!" +
                        " Server IP: " + serverInfo.getServerIp() +
                        ", UDP Port: " + serverInfo.getUdpPort() +
                        ", TCP Port: " + serverInfo.getTcpPort();  // Thêm TCP port vào phản hồi
                sendResponseUDP(response, packet);
            } else {
                sendResponseUDP("Server info not found.", packet);
            }
        } else {
            sendResponseUDP("Login failed. Invalid email or password.", packet);
        }
    }

    private void handleSendEmail(List<String> tokens, DatagramPacket packet) throws IOException {
        String sender = tokens.remove(0);
        String receiver = tokens.remove(0);
        String subject = tokens.remove(0);
        String content = tokens.remove(0);
        String attachmentData = tokens.isEmpty() ? "" : tokens.remove(0);

        try {
            // 1. Lưu email vào CSDL
            Mail mail = new Mail();
            mail.setSender(sender);
            mail.setReceiver(receiver);
            mail.setSubject(subject);
            mail.setContent(content);
            mail.setSentDate(new java.util.Date());
            mail.setSent(true);

            mailDAO.addMail(mail); // Lưu email, trả về mail_id

            // 3. Phản hồi thành công
            sendResponseUDP("Email sent successfully", packet);

        } catch (Exception e) {
            e.printStackTrace();
            sendResponseUDP("Failed to send email: " + e.getMessage(), packet);
        }
    }


    private void handleLoadEmails(List<String> tokens, DatagramPacket packet) throws IOException {
        if (tokens.size() < 1) {
            sendResponseUDP("Invalid load emails request", packet);
            return;
        }
        String email = tokens.remove(0);
        String emails = mailDAO.getAllMailsForUser(email);
        sendResponseUDP(emails.isEmpty() ? "No emails found" : emails, packet);
    }

    private void sendResponseUDP(String response, DatagramPacket packet) throws IOException {
        byte[] buf = response.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
        socket.send(responsePacket);
        view.appendLog("Sent response: " + response);
    }

    private void sendResponseTCP(String response, Socket clientSocket) throws IOException {
        try (OutputStream outputStream = clientSocket.getOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
            // Gửi phản hồi qua TCP
            dataOutputStream.writeUTF(response);
            dataOutputStream.flush();  // Đảm bảo phản hồi được gửi ngay lập tức
        } catch (IOException e) {
            view.appendLog("Error sending response to client: " + e.getMessage());
            e.printStackTrace();
        }
    }

}