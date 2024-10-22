package server;

import javax.swing.*;
import java.awt.*;

public class ServerView extends JFrame {
    private JTextArea logArea;

    public ServerView() {
        setTitle("Mail Server");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
        setVisible(true);
    }

    public void appendLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength()); // Cuộn xuống dưới cùng
    }
}
