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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MailServer {
    private static final int UDP_PORT = 4445;
    private static final int TCP_PORT = 5555; // Cổng TCP
    private DatagramSocket socket;
    private ServerSocket tcpServerSocket;
    private ExecutorService executor;
    private boolean isRunning = false;
    private ServerView view;
    private UserDAO userDAO;
    private MailDAO mailDAO;
    private ServerDAO serverDAO;
    private AttachmentDAO attachmentDAO;

    public MailServer(UserDAO userDAO, MailDAO mailDAO, ServerDAO serverDAO, AttachmentDAO attachmentDAO) {
        this.userDAO = userDAO;
        this.mailDAO = mailDAO;
        this.serverDAO = serverDAO;
        this.attachmentDAO = attachmentDAO;
        this.executor = Executors.newFixedThreadPool(10);  // Tạo một ExecutorService với 10 luồng
    }

    public void setView(ServerView view) {
        this.view = view;
    }

    public void start() {
        if (isRunning) {
            view.appendLog("Server is already running.");
            return;
        }

        try {
            // Khởi tạo UDP server
            socket = new DatagramSocket(UDP_PORT);
            view.appendLog("Mail server is running on UDP port " + UDP_PORT);

            // Khởi tạo TCP server
            tcpServerSocket = new ServerSocket(TCP_PORT);
            view.appendLog("Mail server is running on TCP port " + TCP_PORT);

            // Lưu địa chỉ IP và port của server vào CSDL
            String serverIp = InetAddress.getLocalHost().getHostAddress();
            serverDAO.saveServer(serverIp, UDP_PORT, TCP_PORT); // Lưu cả UDP và TCP port vào CSDL

            // Chạy UDP server trong một luồng riêng biệt
            new Thread(this::startUDPServer).start();

            // Chạy TCP server trong một luồng riêng biệt
            new Thread(this::startTCPServer).start();

            isRunning = true;
            view.appendLog("Server started successfully.");

        } catch (IOException e) {
            view.appendLog("Error starting the server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void startUDPServer() {
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String request = new String(packet.getData(), 0, packet.getLength()).trim();
                view.appendLog("Received UDP request: " + request);

                // Lấy địa chỉ IP và cổng của client gửi request
                String clientIp = packet.getAddress().getHostAddress(); // Địa chỉ IP client
                int clientPort = packet.getPort(); // Cổng của client

                // Tạo một chuỗi duy nhất để xác định client (bao gồm IP và port)
                String clientIdentifier = clientIp + ":" + clientPort;

                // Kiểm tra nếu client chưa có trong sidebar thì mới thêm vào
                if (!view.isClientAlreadyAdded(clientIdentifier)) {
                    // Hiển thị client trong sidebar
                    view.addClient(clientIdentifier);  // Thêm client vào sidebar
                }

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

    private void startTCPServer() {
        try {
            while (true) {
                // Chấp nhận kết nối TCP
                Socket clientSocket = tcpServerSocket.accept();
                String clientIp = clientSocket.getInetAddress().getHostAddress();
                view.appendLog("New TCP connection from " + clientIp);

                // Thêm client vào sidebar
                String clientIdentifier = clientIp + ":" + clientSocket.getPort(); // Lưu địa chỉ và cổng client
                view.addClient(clientIdentifier); // Hiển thị client trong sidebar

                // Xử lý yêu cầu TCP trong một thread riêng
                executor.submit(() -> {
                    try {
                        handleTCPRequest(clientSocket);  // Xử lý kết nối TCP (gửi/nhận tệp)
                    } finally {
                        // Khi client ngắt kết nối, xóa khỏi sidebar
                        view.removeClient(clientIdentifier);  // Loại bỏ client khỏi sidebar
                        try {
                            clientSocket.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        } catch (IOException e) {
            view.appendLog("Error in TCP server: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void stop() {
        if (!isRunning) {
            view.appendLog("Server is not running.");
            return;
        }

        try {
            appendLog("Attempting to stop the server...");

            // Đóng các socket
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            if (tcpServerSocket != null && !tcpServerSocket.isClosed()) {
                tcpServerSocket.close();
            }

            view.appendLog("Mail server stopped successfully.");

            // Xóa địa chỉ IP và port của server khỏi CSDL
            String serverIp = InetAddress.getLocalHost().getHostAddress();
            boolean isDeleted = serverDAO.deleteServer(serverIp, UDP_PORT);

            if (isDeleted) {
                view.appendLog("Server information removed from database.");
            } else {
                view.appendLog("Failed to remove server information from database.");
            }

        } catch (IOException e) {
            view.appendLog("Error stopping server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isRunning = false;
            updateUIForRunningState(false);
            appendLog("Server status updated to 'Stopped'.");
        }
    }

    private void handleTCPRequest(Socket clientSocket) {
        try (InputStream input = clientSocket.getInputStream();
             OutputStream output = clientSocket.getOutputStream();
             DataInputStream dataInputStream = new DataInputStream(input);
             DataOutputStream dataOutputStream = new DataOutputStream(output)) {

            // Đọc lệnh từ client
            String command = dataInputStream.readUTF();
            System.out.println("Received TCP command: " + command);

            switch (command) {
                case "SEND_EMAIL_ATTACHMENT":
                    // Lấy email người gửi và người nhận từ gói tin
                    String senderEmail = dataInputStream.readUTF();
                    String receiverEmail = dataInputStream.readUTF();
                    System.out.println("Sender: " + senderEmail + ", Receiver: " + receiverEmail);

                    // Truy vấn IP và Port của người nhận từ cơ sở dữ liệu
                    String[] ipAndPort = userDAO.getUserIpAndPort(receiverEmail);
                    if (ipAndPort != null) {
                        String recipientIp = ipAndPort[0];
                        String recipientPort = ipAndPort[1];
                        System.out.println("Recipient IP: " + recipientIp + ", Port: " + recipientPort);
                    } else {
                        System.out.println("No IP and Port found for recipient with email: " + receiverEmail);
                    }

                    // Tiếp tục xử lý tệp đính kèm...
                    handleFileUpload(dataInputStream, dataOutputStream);
                    break;

                default:
                    // Lệnh không hợp lệ
                    dataOutputStream.writeUTF("Unknown command: " + command);
                    break;
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }


    private void handleFileUpload(DataInputStream dis, DataOutputStream dos) throws IOException, SQLException {
        int fileCount = dis.readInt(); // Nhận số lượng tệp
        List<Attachment> attachments = new ArrayList<>(); // Danh sách tệp đính kèm để lưu vào DB

        for (int i = 0; i < fileCount; i++) {
            String fileName = dis.readUTF(); // Nhận tên tệp
            long fileSize = dis.readLong(); // Nhận kích thước tệp

            // Tạo thư mục lưu trữ tệp nếu chưa tồn tại
            File attachmentsDir = new File("attachments");
            if (!attachmentsDir.exists() && !attachmentsDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + attachmentsDir.getAbsolutePath());
            }

            // Lưu tệp vào thư mục "attachments"
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

            // Tạo đối tượng Attachment để lưu vào cơ sở dữ liệu
            Attachment attachment = new Attachment();
            attachment.setFileName(fileName);
            attachment.setFilePath(file.getAbsolutePath()); // Lưu đường dẫn đầy đủ đến tệp
            attachment.setFileSize(fileSize);
            attachment.setFileType(Files.probeContentType(Paths.get(file.getAbsolutePath()))); // Lấy loại tệp (MIME type)

            attachments.add(attachment);

            // Gửi phản hồi rằng tệp đã được nhận
            dos.writeUTF("FILE_RECEIVED");
        }

        // Sử dụng AttachmentDAO đã được khởi tạo sẵn ở đâu đó (không cần khởi tạo lại trong phương thức này)
        boolean success = attachmentDAO.addAttachments(attachments); // Giả sử attachmentDAO đã được khởi tạo

        // Gửi phản hồi cuối cùng
        if (success) {
            dos.writeUTF("ALL_FILES_RECEIVED_AND_SAVED");
        } else {
            dos.writeUTF("ERROR_SAVING_FILES");
        }
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
        // Kiểm tra số lượng tokens để đảm bảo yêu cầu đăng nhập hợp lệ
        if (tokens.size() < 2) {
            sendResponseUDP("Invalid login request", packet);
            return;
        }

        String email = tokens.remove(0); // Lấy email
        String password = tokens.remove(0); // Lấy password

        // Kiểm tra xem người dùng đã đăng nhập hay chưa
        if (userDAO.isUserLoggedIn(email)) {
            sendResponseUDP("You are already logged in.", packet);
            return;
        }

        // Tạo đối tượng User để kiểm tra đăng nhập
        User loginUser = new User(0, null, password, email, false);  // isLogin mặc định là false
        boolean loginSuccess = userDAO.loginUser(loginUser);

        if (loginSuccess) {
            // Cập nhật trạng thái đăng nhập của người dùng
            userDAO.updateLoginStatus(email, true);  // Cập nhật isLogin = true

            // Lấy địa chỉ IP và cổng của client từ DatagramPacket
            String clientIp = packet.getAddress().getHostAddress();
            int clientPort = packet.getPort();

            // Cập nhật địa chỉ IP và cổng của người dùng trong cơ sở dữ liệu
            userDAO.updateUserIpAddress(email, clientIp, clientPort);

            // Lấy thông tin máy chủ từ database (UDP và TCP port)
            Server serverInfo = serverDAO.getServerIpAndPort(); // Lấy thông tin máy chủ từ DB (UDP + TCP)

            // Lấy tên người dùng từ database
            String username = userDAO.getUsername(email);

            // Kiểm tra và xử lý thông tin server
            if (serverInfo != null) {
                // Tạo phản hồi có chứa thông tin máy chủ và client
                String response = "Login successful. Welcome, " + username + "!" +
                        " Server IP: " + serverInfo.getServerIp() +
                        ", UDP Port: " + serverInfo.getUdpPort() +
                        ", TCP Port: " + serverInfo.getTcpPort() +
                        ", Your IP: " + clientIp +
                        ", Your Port: " + clientPort;  // Gửi thông tin máy chủ và client vào phản hồi

                // Gửi phản hồi về cho client
                sendResponseUDP(response, packet);
            } else {
                // Nếu không tìm thấy thông tin server trong DB
                sendResponseUDP("Server info not found.", packet);
            }
        } else {
            // Nếu đăng nhập không thành công
            sendResponseUDP("Login failed. Invalid email or password.", packet);
        }
    }


    private void handleSendEmail(List<String> tokens, DatagramPacket packet) throws IOException {
        String sender = tokens.remove(0);
        String receiver = tokens.remove(0);
        String subject = tokens.remove(0);
        String content = tokens.remove(0);

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
    private void appendLog(String message) {
        String timeStampedMessage = String.format("[%s] %s", new SimpleDateFormat("HH:mm:ss").format(new Date()), message);
        view.appendLog(timeStampedMessage);
    }

    private void updateUIForRunningState(boolean isRunning) {
        view.updateUIForRunningState(isRunning);
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