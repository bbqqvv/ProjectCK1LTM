package client;

import java.io.*;
import java.net.*;

public class MailClient {
    private DatagramSocket udpSocket;
    private InetAddress serverAddress;
    private int udpPort;

    public MailClient(String serverAddress, int udpPort) throws Exception {
        this.udpSocket = new DatagramSocket();
        this.serverAddress = InetAddress.getByName(serverAddress);
        this.udpPort = udpPort;
    }

    public String sendRequest(String command, String data, boolean useTcp, File[] files) throws IOException {

            return sendRequestUdp(command, data);

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


    public void close() {
        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }
    }
}
