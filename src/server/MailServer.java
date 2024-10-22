package server;

import model.User;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.StringTokenizer;

import dao.MailDAO;
import dao.UserDAO;

public class MailServer {
    private static final int PORT = 4445;
    private DatagramSocket socket;
    private ServerView view;
    private UserDAO userDAO = new UserDAO();
    private MailDAO mailDAO = new MailDAO();

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
                String request = new String(packet.getData(), 0, packet.getLength());
                view.appendLog("Received: " + request);
                handleRequest(request, packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(String request, DatagramPacket packet) throws IOException {
        StringTokenizer st = new StringTokenizer(request, ":");
        String command = st.nextToken();

        switch (command) {
            case "REGISTER":
                String username = st.nextToken();
                String password = st.nextToken();
                User newUser = new User(username, password);
                boolean registrationSuccess = userDAO.registerUser(newUser);
                sendResponse(registrationSuccess ? "Register successful" : "Register failed", packet);
                break;
            case "LOGIN":
                username = st.nextToken();
                password = st.nextToken();
                User loginUser = new User(username, password);
                boolean loginSuccess = userDAO.loginUser(loginUser);
                sendResponse(loginSuccess ? "Login successful" : "Login failed", packet);
                break;
            case "SEND_EMAIL":
                String sender = st.nextToken();
                String receiver = st.nextToken();
                String subject = st.nextToken(); // Lấy subject từ request
                String content = st.nextToken(); // Lấy content từ request
                boolean emailSent = mailDAO.saveEmail(sender, receiver, subject, content); // Cập nhật phương thức để gửi email
                sendResponse(emailSent ? "Email sent" : "Failed to send email", packet);
                break;
            case "LOAD_EMAILS":
                username = st.nextToken();
                String emails = mailDAO.loadEmails(username);
                sendResponse(emails.isEmpty() ? "No emails found" : emails, packet);
                break;
            default:
                sendResponse("Invalid command", packet);
                break;
        }
    }

    private void sendResponse(String response, DatagramPacket packet) throws IOException {
        byte[] buf = response.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
        socket.send(responsePacket);
        view.appendLog("Sent response: " + response);
    }
}
