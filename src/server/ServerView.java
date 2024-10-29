package server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ServerView extends JFrame {
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;

    public ServerView() {
        setTitle("Mail Server");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());

        // Create toolbar for controls
        JToolBar toolBar = new JToolBar();
        toolBar.setBackground(new Color(173, 216, 230));
        
        startButton = new JButton("Start Server");
        startButton.addActionListener(this::startServer);
        toolBar.add(startButton);
        
        stopButton = new JButton("Stop Server");
        stopButton.addActionListener(this::stopServer);
        toolBar.add(stopButton);
        
        getContentPane().add(toolBar, BorderLayout.NORTH);

        // Create log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(240, 248, 255));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Make window visible
        setVisible(true);
    }

    private void startServer(ActionEvent e) {
        // Logic to start the server
        appendLog("Server started...");
    }

    private void stopServer(ActionEvent e) {
        // Logic to stop the server
        appendLog("Server stopped...");
    }

    public void appendLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength()); // Scroll to the bottom
    }
}
