package model;

public class Server {
	private int serverId;
	private String serverIp;      // Địa chỉ IP của server
	private int udpPort;          // Cổng UDP

	// Constructor đầy đủ tham số
	public Server(int serverId, String serverIp, int udpPort) {
		this.serverId = serverId;
		this.serverIp = serverIp;
		this.udpPort = udpPort;
	}

	public Server(String serverIp, int udpPort) {
		this.serverIp = serverIp;
		this.udpPort = udpPort;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getServerId() {
		return serverId;
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


}
