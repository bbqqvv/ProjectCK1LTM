package client;

import javax.swing.*;
import dao.ServerDAO;
import dao.UserDAO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;

public class SettingsDialog extends JDialog {

    private int emailsPerPage;
    private String selectedTheme;
    private String selectedFontSize;
    private String selectedSortOrder;
    private boolean notificationsEnabled;
    private boolean autoRefreshEnabled;
    private String userEmail;
    private MailClientView parentView;
    private ServerDAO serverDAO;
    private UserDAO userDAO;

    // Constructor only with parentView, leaving the rest for setters
    public SettingsDialog(MailClientView parentView, UserDAO userDAO) {
        super(parentView, "Settings", true);
        this.parentView = parentView;
        this.userEmail = parentView.getUserEmail();  // Gán userEmail từ parentView
        this.userDAO = userDAO;
        initializeDialog();
    }


//    // Setters to initialize other values
//    public void initializeSettings(int emailsPerPage, String userEmail, UserDAO userDAO, ServerDAO serverDAO) {
//        this.emailsPerPage = emailsPerPage;
//        this.userEmail = userEmail;
//        this.userDAO = userDAO;  // Gán userDAO từ ngoài vào
//        this.serverDAO = serverDAO;  // Gán serverDAO
//    }

    private void initializeDialog() {
        setSize(550, 400);
        setLocationRelativeTo(parentView);
        setLayout(new BorderLayout(10, 10));

        JPanel settingsPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Components for settings
        JComboBox<String> themeComboBox = new JComboBox<>(new String[]{"Light", "Dark", "Auto"});
        JTextField emailsPerPageField = new JTextField(String.valueOf(emailsPerPage));
        JComboBox<String> fontSizeComboBox = new JComboBox<>(new String[]{"Small", "Medium", "Large"});
        JComboBox<String> sortOrderComboBox = new JComboBox<>(new String[]{"Date", "Subject", "Sender"});
        JCheckBox notificationsCheckBox = new JCheckBox("Enable Notifications", notificationsEnabled);
        JCheckBox autoRefreshCheckBox = new JCheckBox("Enable Auto Refresh", autoRefreshEnabled);
        JTextField usernameField = new JTextField(userEmail);
        JComboBox<String> languageComboBox = new JComboBox<>(new String[]{"English", "Vietnamese"});

        // Language selection listener
        languageComboBox.addActionListener(e -> {
            String selectedLanguage = (String) languageComboBox.getSelectedItem();
            String message = selectedLanguage.equals("Vietnamese") ? "Language set to Vietnamese." : "Language set to English.";
            JOptionPane.showMessageDialog(this, message);
        });

        // Add components to settings panel
        settingsPanel.add(new JLabel("Select Theme"));
        settingsPanel.add(themeComboBox);
        settingsPanel.add(new JLabel("Emails per Page"));
        settingsPanel.add(emailsPerPageField);
        settingsPanel.add(new JLabel("Font Size"));
        settingsPanel.add(fontSizeComboBox);
        settingsPanel.add(new JLabel("Sort Order"));
        settingsPanel.add(sortOrderComboBox);
        settingsPanel.add(new JLabel("Notifications"));
        settingsPanel.add(notificationsCheckBox);
        settingsPanel.add(new JLabel("Auto Refresh"));
        settingsPanel.add(autoRefreshCheckBox);
        settingsPanel.add(new JLabel("Current Email: " + userEmail));
        settingsPanel.add(usernameField);
        settingsPanel.add(new JLabel("Language"));
        settingsPanel.add(languageComboBox);

        // Save Button with an icon
        JButton saveButton = new JButton("Save");
        saveButton.setPreferredSize(new Dimension(120, 40));
        saveButton.addActionListener(e -> {
            selectedTheme = (String) themeComboBox.getSelectedItem();
            selectedFontSize = (String) fontSizeComboBox.getSelectedItem();
            selectedSortOrder = (String) sortOrderComboBox.getSelectedItem();
            notificationsEnabled = notificationsCheckBox.isSelected();
            autoRefreshEnabled = autoRefreshCheckBox.isSelected();
            String newUsername = usernameField.getText();

            int emailsPerPageValue;
            try {
                emailsPerPageValue = Integer.parseInt(emailsPerPageField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(parentView, "Invalid value for Emails per Page.", "Invalid Input",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Apply changes
            applyTheme(selectedTheme);
            updateFontSize(selectedFontSize);
            updateNotifications(notificationsEnabled);
            updateAutoRefresh(autoRefreshEnabled);
            updateUsername(newUsername);

            dispose();
        });

        // Reset Button
        JButton resetButton = new JButton("Reset");
        resetButton.setPreferredSize(new Dimension(150, 40));
        resetButton.addActionListener(e -> {
            themeComboBox.setSelectedIndex(0); // Light theme
            fontSizeComboBox.setSelectedIndex(1); // Medium font
            sortOrderComboBox.setSelectedIndex(0); // Date sort
            notificationsCheckBox.setSelected(true);
            autoRefreshCheckBox.setSelected(false);
            emailsPerPageField.setText("20");
            usernameField.setText(userEmail);
        });

        // Logout Button
        JButton logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(120, 40));

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (userEmail == null || userEmail.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No user detected.");
                    return;
                }

                if (userDAO.isUserLoggedIn(userEmail)) {
                    userDAO.updateLoginStatus(userEmail, false);
                    JOptionPane.showMessageDialog(null, "Logout successful.");
                    // Safeguard if parentView is null
                    if (parentView != null) {
                        parentView.showLoginScreen();
                    } else {
                        JOptionPane.showMessageDialog(null, "Error: Parent view is null.");
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "User is not logged in.");
                }
            }
        });

        settingsPanel.add(logoutButton);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(saveButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(logoutButton);

        // Add components to the dialog
        add(settingsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void applyTheme(String selectedTheme) {
        if ("Dark".equalsIgnoreCase(selectedTheme)) {
            parentView.getContentPane().setBackground(Color.DARK_GRAY);
            parentView.updateStatusLabel("Dark theme applied.");
        } else if ("Auto".equalsIgnoreCase(selectedTheme)) {
            applyAutoTheme();
        } else {
            parentView.getContentPane().setBackground(Color.WHITE);
            parentView.updateStatusLabel("Light theme applied.");
        }
        SwingUtilities.updateComponentTreeUI(parentView);
    }

    private void applyAutoTheme() {
        LocalTime currentTime = LocalTime.now();
        if (currentTime.isBefore(LocalTime.NOON)) {
            applyTheme("Light");
        } else {
            applyTheme("Dark");
        }
    }

    private void updateFontSize(String fontSize) {
        int fontSizeValue;
        switch (fontSize) {
            case "Small":
                fontSizeValue = 12;
                break;
            case "Medium":
                fontSizeValue = 14;
                break;
            case "Large":
                fontSizeValue = 18;
                break;
            default:
                fontSizeValue = 14;
                break;
        }
        parentView.setFont(new Font("Arial", Font.PLAIN, fontSizeValue));
    }

    private void updateNotifications(boolean notificationsEnabled) {
        parentView.setNotificationsEnabled(notificationsEnabled);
        if (notificationsEnabled) {
            parentView.updateStatusLabel("Notifications enabled.");
        } else {
            parentView.updateStatusLabel("Notifications disabled.");
        }
    }

    private void updateAutoRefresh(boolean autoRefreshEnabled) {
        parentView.setAutoRefreshEnabled(autoRefreshEnabled);
        if (autoRefreshEnabled) {
            parentView.updateStatusLabel("Auto refresh enabled.");
        } else {
            parentView.updateStatusLabel("Auto refresh disabled.");
        }
    }

    private void updateUsername(String newUsername) {
        parentView.setUsername(newUsername);
        parentView.updateStatusLabel("Username updated to: " + newUsername);
    }
}
