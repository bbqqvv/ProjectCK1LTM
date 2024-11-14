package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SidebarPanel extends JPanel {
    private MailClientView mailClientView;

    public SidebarPanel(MailClientView mailClientView) {
        this.mailClientView = mailClientView;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add buttons to the sidebar
        addButton("✉ Send Email", e -> mailClientView.switchPanel("SendEmail"));
        addButton("📥 Load Emails", e -> mailClientView.switchPanel("LoadEmails"));
        addButton("🗑️ Delete Email", e -> mailClientView.deleteEmail());
        addButton("↩️ Reply Email", e -> mailClientView.replyEmail());
        addButton("💬 Chat", e -> mailClientView.switchPanel("Chat"));  // Nút mới cho Chat
        addButton("⚙ Settings", e -> mailClientView.openSettings());  // Nút mới cho Settings

    }

    private void addButton(String text, ActionListener action) {
        JButton button = createButton(text);
        button.addActionListener(action);
        add(button);
        add(Box.createVerticalStrut(10));
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 40));
        button.setFocusPainted(false);
        button.setBackground(new Color(255, 223, 0)); // Màu vàng sáng
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorderPainted(false);
        button.addMouseListener(new ButtonHoverEffect());
        button.setToolTipText("Click to " + text);
        return button;
    }

    class ButtonHoverEffect extends MouseAdapter {
        @Override
        public void mouseEntered(MouseEvent e) {
            e.getComponent().setBackground(new Color(255, 204, 0)); // Màu vàng đậm khi hover
        }

        @Override
        public void mouseExited(MouseEvent e) {
            e.getComponent().setBackground(new Color(255, 223, 0)); // Trở lại màu vàng sáng khi không hover
        }
    }
}
