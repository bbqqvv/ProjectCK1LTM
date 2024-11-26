package model;

public class Server {
	private String serverId;      // UUID để định danh server
	private String serverIp;      // Địa chỉ IP của server
	private int udpPort;          // Cổng UDP
	private int tcpPort;          // Cổng TCP

	// Constructor đầy đủ tham số
	public Server(String serverId, String serverIp, int udpPort, int tcpPort) {
		this.serverId = serverId;
		this.serverIp = serverIp;
		this.udpPort = udpPort;
		this.tcpPort = tcpPort;
	}

	// Constructor không có serverId (dùng khi thêm server mới)
	public Server(String serverIp, int udpPort, int tcpPort) {
		this(null, serverIp, udpPort, tcpPort);
	}

	// Constructor chỉ có IP và UDP port (trường hợp không dùng TCP)
	public Server(String serverIp, int udpPort) {
		this(null, serverIp, udpPort, -1); // TCP port chưa được thiết lập
	}

	// Getters và Setters
	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public int getUdpPort() {
		return udpPort;
	}

	public void setUdpPort(int udpPort) {
		this.udpPort = udpPort;
	}

	public int getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}

	// Phương thức kiểm tra nếu TCP port chưa được thiết lập
	public boolean hasTcpPort() {
		return tcpPort > 0;
	}

	// Phương thức tiện ích để hiển thị thông tin server
	@Override
	public String toString() {
		return "Server{" +
				"serverId='" + (serverId != null ? serverId : "N/A") + '\'' +
				", serverIp='" + serverIp + '\'' +
				", udpPort=" + udpPort +
				", tcpPort=" + (tcpPort > 0 ? tcpPort : "N/A") +
				'}';
	}
}
