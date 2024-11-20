package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SidebarPanel extends JPanel {
    private MailClientView mailClientView;
    private JPanel buttonPanel;
    private boolean sidebarCollapsed = false;

    // Constants for configuration
    private static final int EXPANDED_WIDTH = 200;
    private static final int COLLAPSED_WIDTH = 60;
    private static final int BUTTON_HEIGHT = 40;
    private static final Color BACKGROUND_COLOR = new Color(30, 32, 35);
    private static final Color BUTTON_COLOR = new Color(50, 50, 50);
    private static final Color BUTTON_HOVER_COLOR = new Color(65, 65, 65);
    private static final Color MENU_BUTTON_COLOR = new Color(44, 46, 49);
    private static final Color MENU_BUTTON_HOVER_COLOR = new Color(58, 60, 63);
    private static final Color TEXT_COLOR = Color.WHITE;

    private Timer resizeTimer;

    // Constructor
    public SidebarPanel(MailClientView mailClientView) {
        this.mailClientView = mailClientView;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setPreferredSize(new Dimension(EXPANDED_WIDTH, getHeight()));

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add buttons to switch between panels
        addButtonFromImage("src/images/mail.png", "Send Email", "Send Email", e -> mailClientView.switchPanel("SendEmail"));
        addButtonFromImage("src/images/loading.png", "Load Emails", "Load Emails", e -> mailClientView.switchPanel("EmailList"));
        addButtonFromImage("src/images/delete-message.png", "Delete Email", "Delete Email", e -> mailClientView.deleteEmail());
        addButtonFromImage("src/images/arrow.png", "Reply Email", "Reply Email", e -> mailClientView.replyEmail());
        addButtonFromImage("src/images/chat.png", "Chat", "Chat", e -> mailClientView.switchPanel("Chat"));
        addButtonFromImage("src/images/gear.png", "Settings", "Settings", e -> mailClientView.openSettings());

        // Add hamburger menu button at the top of the sidebar
        createButtonMenu();

        // Add the button panel to the sidebar
        add(buttonPanel, BorderLayout.CENTER);
    }

    // Method to create buttons with icon and text
    private JButton createSidebarButton(String imagePath, String tooltip, String text, ActionListener action) {
        ImageIcon icon = new ImageIcon(imagePath);
        if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
            System.err.println("Image not found: " + imagePath);
            return null;
        }
        Image image = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        icon = new ImageIcon(image);

        // Button with both icon and text
        JButton button = new JButton(icon);
        button.setToolTipText(tooltip);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, BUTTON_HEIGHT));
        button.setFocusPainted(false);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Set button text for expanded mode
        if (!sidebarCollapsed) {
            button.setText(text);
        }

        // Horizontal BoxLayout to align text and icon neatly
        button.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        button.setHorizontalAlignment(SwingConstants.LEFT); // Align text to the left

        // Add hover effect for button
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BUTTON_COLOR);
            }
        });

        button.addActionListener(action);
        return button;
    }

    // Method to add button to sidebar
    private void addButtonFromImage(String imagePath, String tooltip, String text, ActionListener action) {
        JButton button = createSidebarButton(imagePath, tooltip, text, action);
        if (button != null) {
            buttonPanel.add(button);
            buttonPanel.add(Box.createVerticalStrut(10)); // Consistent spacing between buttons
        }
    }

    // Create hamburger menu button
    private void createButtonMenu() {
        JButton buttonMenu = new JButton(new ImageIcon(getClass().getResource("/images/main-menu.png")));
        buttonMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonMenu.setPreferredSize(new Dimension(50, 50));
        buttonMenu.setFocusPainted(false);
        buttonMenu.setBackground(MENU_BUTTON_COLOR);
        buttonMenu.setForeground(Color.WHITE);
        buttonMenu.setFont(new Font("Arial", Font.BOLD, 18));
        buttonMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buttonMenu.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add hover effect for menu button
        buttonMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                buttonMenu.setBackground(MENU_BUTTON_HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                buttonMenu.setBackground(MENU_BUTTON_COLOR);
            }
        });

        // Add action when clicking the menu button
        buttonMenu.addActionListener(e -> toggleSidebar());

        // Add menu button to the top of the sidebar
        add(buttonMenu, BorderLayout.NORTH);
    }

    // This method will collapse or expand the sidebar with easing effect
    private void toggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        int targetWidth = sidebarCollapsed ? COLLAPSED_WIDTH : EXPANDED_WIDTH;

        // Cancel any existing timer
        if (resizeTimer != null && resizeTimer.isRunning()) {
            resizeTimer.stop();
        }

        // Smooth resizing with animation
        resizeTimer = new Timer(5, e -> {
            int currentWidth = getWidth();
            int delta = (targetWidth - currentWidth) / 10;

            if (Math.abs(targetWidth - currentWidth) <= Math.abs(delta)) {
                setPreferredSize(new Dimension(targetWidth, getHeight()));
                ((Timer) e.getSource()).stop();
                updateButtonVisibility();
            } else {
                setPreferredSize(new Dimension(currentWidth + delta, getHeight()));
            }

            revalidate();
            repaint();
        });

        resizeTimer.start();
    }

    // Update button visibility with smooth transition
    private void updateButtonVisibility() {
        for (Component comp : buttonPanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                button.setText(sidebarCollapsed ? "" : button.getToolTipText());
            }
        }
    }
}
