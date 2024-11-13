package client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MailClient {
    private DatagramSocket socket;
    private InetAddress address;
    private int port;

    public MailClient(String serverAddress, int serverPort) throws Exception {
        socket = new DatagramSocket();
        address = InetAddress.getByName(serverAddress);
        port = serverPort;
    }

    public String sendRequest(String request) throws Exception {
        byte[] buf = request.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);

        byte[] receiveBuf = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
        socket.receive(receivePacket);

        return new String(receivePacket.getData(), 0, receivePacket.getLength());
    }

    public void close() {
        socket.close();
    }
}