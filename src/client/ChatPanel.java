package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatPanel extends JPanel {
    private JTextArea chatArea;      // Hiển thị tin nhắn
    private JTextField messageField; // Nhập tin nhắn
    private JButton sendButton;      // Nút gửi tin nhắn
    private JButton attachButton;    // Nút gửi file
    private JButton emojiButton;     // Nút chọn emoji
    private JScrollPane scrollPane;  // Cuộn chatArea nếu nhiều tin nhắn
    private JPanel inputPanel;       // Panel cho ô nhập và nút
    private JPanel headerPanel;      // Header cho các công cụ chat

    public ChatPanel(MailClientView parent) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));

        // Tạo chatArea với dòng chỉnh sửa không thể thay đổi
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(245, 245, 245));
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setMargin(new Insets(10, 10, 10, 10)); // Khoảng cách giữa văn bản và viền

        // Đặt JScrollPane cho chatArea
        scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        // Tạo headerPanel cho các nút công cụ chat (biểu tượng cảm xúc, đính kèm file)
        headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(Color.WHITE);

        // Nút biểu tượng cảm xúc
        emojiButton = new JButton("😊");
        emojiButton.setFont(new Font("Arial", Font.PLAIN, 20));
        emojiButton.setBackground(Color.WHITE);
        emojiButton.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        headerPanel.add(emojiButton);

        // Nút đính kèm file
        attachButton = new JButton("Attach");
        attachButton.setFont(new Font("Arial", Font.PLAIN, 12));
        attachButton.setBackground(Color.WHITE);
        attachButton.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        headerPanel.add(attachButton);

        // Thêm headerPanel vào phía trên cùng
        add(headerPanel, BorderLayout.NORTH);

        // Tạo inputPanel cho nhập tin nhắn và nút gửi
        inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.setBackground(Color.WHITE);

        // Tạo ô nhập tin nhắn
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageField.setBackground(new Color(250, 250, 250));
        messageField.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        inputPanel.add(messageField, BorderLayout.CENTER);

        // Nút gửi tin nhắn
        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(0, 122, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Thêm inputPanel vào phía dưới
        add(inputPanel, BorderLayout.SOUTH);

        // Lắng nghe sự kiện khi người dùng nhấn nút gửi
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Lắng nghe sự kiện khi người dùng nhấn phím Enter để gửi tin nhắn
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Lắng nghe sự kiện khi người dùng nhấn nút đính kèm file
        attachButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendFile();
            }
        });

        // Lắng nghe sự kiện khi người dùng nhấn nút emoji
        emojiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                insertEmoji();
            }
        });
    }

    // Phương thức gửi tin nhắn
    private void sendMessage() {
        String message = messageField.getText();
        if (!message.trim().isEmpty()) {
            String timestamp = getCurrentTime();
            chatArea.append(formatMessage("You", message, timestamp) + "\n");
            messageField.setText("");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    // Phương thức gửi file đính kèm
    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String fileName = fileChooser.getSelectedFile().getName();
            String timestamp = getCurrentTime();
            chatArea.append(formatMessage("You", "[File Attachment: " + fileName + "]", timestamp) + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    // Phương thức để thêm emoji vào ô nhập liệu
    private void insertEmoji() {
        messageField.setText(messageField.getText() + "😊");
    }

    // Phương thức để nhận tin nhắn
    public void receiveMessage(String message, String sender) {
        String timestamp = getCurrentTime();
        chatArea.append(formatMessage(sender, message, timestamp) + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    // Phương thức lấy thời gian hiện tại
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    // Phương thức định dạng tin nhắn
    private String formatMessage(String sender, String message, String timestamp) {
        return "<" + timestamp + "> " + sender + ": " + message;
    }
}
