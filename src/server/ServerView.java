package server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ServerView extends JFrame {
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private boolean isRunning = false; // Biến trạng thái để theo dõi server
	private ServerView view;

    public ServerView() {
        setTitle("Mail Server");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());

        // Tạo toolbar với các nút điều khiển
        JToolBar toolBar = new JToolBar();
        toolBar.setBackground(new Color(173, 216, 230));
        
        startButton = new JButton("Start Server");
        startButton.addActionListener(this::startServer);
        toolBar.add(startButton);
        
        stopButton = new JButton("Stop Server");
        stopButton.addActionListener(this::stopServer);
        toolBar.add(stopButton);
        
        getContentPane().add(toolBar, BorderLayout.NORTH);

        // Tạo khu vực log
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(240, 248, 255));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Hiển thị cửa sổ
        setVisible(true);
    }

    private void startServer(ActionEvent e) {
        if (isRunning) { 
            // Kiểm tra nếu server đang chạy
            JOptionPane.showMessageDialog(this, "Server is already running!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        isRunning = true; // Cập nhật trạng thái là server đang chạy
        logArea.setText(""); // Xóa log trước khi bắt đầu lại
        appendLog("Server started...");
    }

    private void stopServer(ActionEvent e) {
        if (!isRunning) {
            // Kiểm tra nếu server đã dừng
            JOptionPane.showMessageDialog(this, "Server is not running.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        appendLog("Server stopped...");
        isRunning = false; // Cập nhật trạng thái là server đã dừng
        logArea.setText(""); // Xóa log khi dừng server
    }



    // Phương thức để hiển thị thông báo trên giao diện
    public void showNotification(String message) {
        JOptionPane.showMessageDialog(this, message, "New Notification", JOptionPane.INFORMATION_MESSAGE);
    }



    public void appendLog(String message) {
        // Ghi vào JTextArea
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength()); // Cuộn xuống cuối cùng

        // Ghi log vào file
        try (FileWriter fw = new FileWriter("server.log", true); BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(message + "\n");
        } catch (IOException e) {
            view.appendLog("Error writing log to file: " + e.getMessage());
        }
    }

}
