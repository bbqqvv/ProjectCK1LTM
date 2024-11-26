package client;

import java.io.*;
import java.net.*;

public class MailClient {
    private DatagramSocket udpSocket;
    private InetAddress serverAddress;
    private int udpPort;
    private int tcpPort;

    // Constructor để khởi tạo kết nối tới server
    public MailClient(String serverAddress, int udpPort, int tcpPort) throws Exception {
        // Khởi tạo kết nối UDP
        this.udpSocket = new DatagramSocket();
        this.serverAddress = InetAddress.getByName(serverAddress);
        this.udpPort = udpPort;
        this.tcpPort = tcpPort;
    }

    // Phương thức gửi yêu cầu (cả TCP và UDP)
    public String sendRequest(String command, String data, boolean useTcp, File[] files) throws IOException {
        if (useTcp) {
            return sendRequestTcp(command, files); // Gửi qua TCP với tệp
        } else {
            return sendRequestUdp(command, data); // Gửi qua UDP không có tệp
        }
    }

    // Gửi yêu cầu qua UDP
    private String sendRequestUdp(String command, String data) throws IOException {
        String request = command + ":" + data;
        byte[] buf = request.getBytes();

        DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress, udpPort);
        udpSocket.send(packet);

        byte[] receiveBuf = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
        udpSocket.receive(responsePacket);

        return new String(responsePacket.getData(), 0, responsePacket.getLength());
    }

    // Gửi yêu cầu qua TCP và truyền tệp
    private String sendRequestTcp(String command, File[] files) throws IOException {
        try (Socket socket = new Socket(serverAddress, tcpPort);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {

            // Gửi lệnh và số lượng tệp
            dos.writeUTF(command);
            dos.writeInt(files.length);

            for (File file : files) {
                dos.writeUTF(file.getName());  // Gửi tên tệp
                dos.writeLong(file.length()); // Gửi kích thước tệp

                // Gửi dữ liệu tệp từng phần
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[8192]; // Tăng kích thước buffer để gửi nhanh hơn
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, bytesRead);
                    }
                }

                // Chờ xác nhận từ server sau mỗi tệp
                String response = dis.readUTF();
                if (!"FILE_RECEIVED".equals(response)) {
                    throw new IOException("Server failed to acknowledge file: " + file.getName());
                }
            }

            // Nhận phản hồi cuối cùng từ server
            return dis.readUTF();
        }
    }


    // Đóng kết nối UDP
    public void close() {
        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }
    }
}
