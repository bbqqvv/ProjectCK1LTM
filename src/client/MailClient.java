package client;

import java.io.*;
import java.net.*;

public class MailClient {
    private DatagramSocket udpSocket;
    private InetAddress serverAddress;
    private int udpPort;
    private int tcpPort;

    public MailClient(String serverAddress, int udpPort, int tcpPort) throws Exception {
        this.udpSocket = new DatagramSocket();
        this.serverAddress = InetAddress.getByName(serverAddress);
        this.udpPort = udpPort;
        this.tcpPort = tcpPort;
    }

    public String sendRequest(String command, String data, boolean useTcp, File[] files) throws IOException {
        if (useTcp) {
            return sendRequestTcp(command, files); // Gửi qua TCP (với tệp)
        } else {
            return sendRequestUdp(command, data); // Gửi qua UDP (không tệp)
        }
    }

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


    private String sendRequestTcp(String command, File[] files) throws IOException {
        try (Socket socket = new Socket(serverAddress, tcpPort);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {

            dos.writeUTF(command); // Gửi lệnh
            dos.writeInt(files.length); // Gửi số lượng tệp

            for (File file : files) {
                dos.writeUTF(file.getName()); // Gửi tên tệp
                dos.writeLong(file.length()); // Gửi kích thước tệp

                // Đọc và gửi tệp theo từng khối
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, bytesRead);
                    }
                }

                // Đọc phản hồi từ server sau mỗi tệp
                String response = dis.readUTF();
                if (!"FILE_RECEIVED".equals(response)) {
                    throw new IOException("Server did not acknowledge file: " + file.getName());
                }
            }

            // Nhận phản hồi cuối cùng từ server
            if (dis.available() > 0) { // Kiểm tra xem còn dữ liệu để đọc không
                return dis.readUTF();
            } else {
                throw new EOFException("No final response received from server.");
            }
        }
    }



    public void close() {
        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }
    }
}
