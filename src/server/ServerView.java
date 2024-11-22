package server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import dao.ServerDAO;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

public class ServerView extends JFrame {
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private JButton clearLogButton;
    private JButton saveLogButton;
    private JLabel statusLabel;
    private boolean isRunning = false;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private MailServer mailServer;
    private JPanel sidebarPanel; // Thay đổi từ JScrollPane để dễ quản lý
    private boolean isSidebarVisible = true; // Trạng thái hiển thị của sidebar
    private JButton toggleSidebarButton; // Nút đóng/mở sidebar

    // Sidebar components
    private DefaultListModel<String> clientListModel;
    private JList<String> clientList;

    public ServerView(MailServer mailServer) {
        this.mailServer = mailServer; // Initialize mailServer
        setTitle("Mail Server");
        setSize(900, 500); // Increased width to accommodate the sidebar
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 248, 255));
        initUI();

        setVisible(true);
    }

    private void initUI() {
        initToolBar();
        initLogArea();
        initStatusBar();
        initSidebar();
    }

    private void initToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(new Color(131, 122, 122));
        toolBar.setBorder(new EmptyBorder(5, 5, 5, 5));

        startButton = createButton("Start Server", "Start the server", e -> startServer(e), "/images/play.png", 24, 24);
        stopButton = createButton("Stop Server", "Stop the server", e -> stopServer(e), "/images/stop-button.png", 24, 24);
        clearLogButton = createButton("Clear Log", "Clear the log area", e -> logArea.setText(""), "/images/clean.png", 24, 24);
        saveLogButton = createButton("Save Log", "Save the log to a file", e -> saveLogToFile(e), "/images/diskette.png", 24, 24);
        toggleSidebarButton = createButton("", "Show/Hide sidebar", e -> toggleSidebar(), "/images/menu-bar.png", 24, 24);

        toolBar.add(startButton);
        toolBar.add(stopButton);
        toolBar.addSeparator();
        toolBar.add(clearLogButton);
        toolBar.add(saveLogButton);
        toolBar.addSeparator(new Dimension(400,0) );

        toolBar.add(toggleSidebarButton);

        getContentPane().add(toolBar, BorderLayout.NORTH);
    }

    private void toggleSidebar() {
        if (isSidebarVisible) {
            getContentPane().remove(sidebarPanel); // Xóa sidebar khỏi giao diện
            toggleSidebarButton.setText("");
        } else {
            getContentPane().add(sidebarPanel, BorderLayout.EAST); // Thêm lại sidebar
            toggleSidebarButton.setText("");
        }

        isSidebarVisible = !isSidebarVisible; // Đổi trạng thái hiển thị
        getContentPane().revalidate(); // Cập nhật giao diện
        getContentPane().repaint();   // Vẽ lại giao diện
    }


    private JButton createButton(String text, String toolTipText, ActionListener action, String iconPath, int width, int height) {
        JButton button = new JButton(text);

        try {
            // Load the icon
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));

            // Resize the icon
            Image scaledImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledImage));
        } catch (NullPointerException e) {
            System.out.println("Icon not found: " + iconPath);
        }

        button.setToolTipText(toolTipText);
        button.addActionListener(action);
        return button;
    }

    private void initLogArea() {
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        logArea.setBackground(new Color(255, 255, 255));
        logArea.setForeground(new Color(50, 50, 50));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 230)),
                "📄 Server Log",
                0,
                0,
                new Font("SansSerif", Font.BOLD, 14),
                new Color(80, 80, 80)
        ));

        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                scrollPane.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private void initStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        statusLabel = new JLabel("Status: Stopped", new ImageIcon("icons/stopped.png"), JLabel.LEFT);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(Color.RED);
        statusPanel.add(statusLabel, BorderLayout.WEST);

        getContentPane().add(statusPanel, BorderLayout.SOUTH);
    }

    private void initSidebar() {
        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        clientList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        clientList.setBackground(new Color(245, 245, 245));
        clientList.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 230)),
                "🖧 Connected Clients",
                0,
                0,
                new Font("SansSerif", Font.BOLD, 14),
                new Color(80, 80, 80)
        ));

        JScrollPane sidebarScroll = new JScrollPane(clientList);
        sidebarScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Đặt JScrollPane vào JPanel để quản lý dễ hơn
        sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.add(sidebarScroll, BorderLayout.CENTER);

        // Thêm sidebar vào giao diện chính
        getContentPane().add(sidebarPanel, BorderLayout.EAST);
    }


    private void startServer(ActionEvent e) {
        if (isRunning) {
            showNotification("Server is already running!");
            return;
        }

        isRunning = true;
        logArea.setText("");
        appendLog("Server started...");
        updateUIForRunningState(true);

        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                mailServer.start();
                return null;
            }

            @Override
            protected void done() {
                appendLog("Server is running.");
            }
        }.execute();
    }

    private void stopServer(ActionEvent e) {
        if (!isRunning) {
            showNotification("Server is not running.");
            return;
        }

        try {
            appendLog("Attempting to stop the server...");
            mailServer.stop();
            appendLog("Server stopped successfully.");
        } catch (Exception ex) {
            appendLog("Error stopping server: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            isRunning = false;
            updateUIForRunningState(false);
            appendLog("Server status updated to 'Stopped'.");
        }
    }

    private void saveLogToFile(ActionEvent e) {
        try (FileWriter fw = new FileWriter("server.log", true); BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(logArea.getText());
            showNotification("Log saved to server.log");
        } catch (IOException ex) {
            appendLog("Error saving log to file: " + ex.getMessage());
        }
    }

    public void showNotification(String message) {
        JOptionPane.showMessageDialog(this, message, "Notification", JOptionPane.INFORMATION_MESSAGE);
    }

    public void appendLog(String message) {
        String timeStampedMessage = String.format("[%s] %s", dateFormat.format(new Date()), message);
        logArea.append(timeStampedMessage + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());

        try (FileWriter fw = new FileWriter("server.log", true); BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(timeStampedMessage + "\n");
        } catch (IOException e) {
            logArea.append("Error writing log to file: " + e.getMessage() + "\n");
        }
    }

    public void addClient(String clientName) {
        clientListModel.addElement(clientName);
    }

    public void removeClient(String clientName) {
        clientListModel.removeElement(clientName);
    }

    private void updateUIForRunningState(boolean isRunning) {
        startButton.setEnabled(!isRunning);
        stopButton.setEnabled(isRunning);

        if (isRunning) {
            statusLabel.setText("Status: Running");
            statusLabel.setForeground(new Color(0, 128, 0));
            statusLabel.setIcon(new ImageIcon("icons/running.png"));
        } else {
            statusLabel.setText("Status: Stopped");
            statusLabel.setForeground(Color.RED);
            statusLabel.setIcon(new ImageIcon("icons/stopped.png"));
        }
    }
}
