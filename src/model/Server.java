package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Server {
    private String serverId;
    private String serverIp;
    private int serverPort;

    // Constructor chỉ có IP và Port
    public Server(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }
}
