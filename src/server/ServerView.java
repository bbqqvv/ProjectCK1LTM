package server;

import javax.swing.*;
import java.awt.*;

public class ServerView extends JFrame {
    private JTextArea serverLogs;

    public ServerView() {
        setTitle("Mail Server");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverLogs = new JTextArea();
        serverLogs.setEditable(false);
        add(new JScrollPane(serverLogs), BorderLayout.CENTER);
        setVisible(true);
    }

    public void appendLog(String log) {
        serverLogs.append(log + "\n");
    }
}
