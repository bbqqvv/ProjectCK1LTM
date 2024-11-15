package client;

import javax.swing.*;

import dao.ServerDAO;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.util.Locale;
import java.util.ResourceBundle;

public class SettingsDialog extends JDialog {

    private int emailsPerPage;
    private String selectedTheme;
    private String selectedFontSize;
    private String selectedSortOrder;
    private boolean notificationsEnabled;
    private boolean autoRefreshEnabled;
    private String username;
    private Locale currentLocale;
    private ResourceBundle messages;
    private MailClientView parentView;

    // Constructor
    public SettingsDialog(MailClientView parentView, int emailsPerPage, String username) {
        super(parentView, "Settings", true);
        this.parentView = parentView;
        this.emailsPerPage = emailsPerPage;
        this.username = username;
        this.currentLocale = Locale.getDefault(); // Default locale
        this.messages = ResourceBundle.getBundle("messages", currentLocale); // Load messages for current locale
        initializeDialog();
    }

    private void initializeDialog() {
        setSize(450, 400);
        setLocationRelativeTo(parentView);
        setLayout(new BorderLayout(10, 10));

        JPanel settingsPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel themeLabel = new JLabel(messages.getString("settings.selectTheme"));
        JComboBox<String> themeComboBox = new JComboBox<>(new String[]{messages.getString("settings.light"), messages.getString("settings.dark"), messages.getString("settings.auto")});

        JLabel emailsPerPageLabel = new JLabel(messages.getString("settings.emailsPerPage"));
        JTextField emailsPerPageField = new JTextField(String.valueOf(emailsPerPage));

        JLabel fontSizeLabel = new JLabel(messages.getString("settings.fontSize"));
        JComboBox<String> fontSizeComboBox = new JComboBox<>(new String[]{messages.getString("settings.small"), messages.getString("settings.medium"), messages.getString("settings.large")});

        JLabel sortOrderLabel = new JLabel(messages.getString("settings.sortOrder"));
        JComboBox<String> sortOrderComboBox = new JComboBox<>(new String[]{messages.getString("settings.date"), messages.getString("settings.subject"), messages.getString("settings.sender")});

        JLabel notificationsLabel = new JLabel(messages.getString("settings.notifications"));
        JCheckBox notificationsCheckBox = new JCheckBox(messages.getString("settings.enableNotifications"), notificationsEnabled);

        JLabel autoRefreshLabel = new JLabel(messages.getString("settings.autoRefresh"));
        JCheckBox autoRefreshCheckBox = new JCheckBox(messages.getString("settings.enableAutoRefresh"), autoRefreshEnabled);

        JLabel usernameLabel = new JLabel(messages.getString("settings.currentUsername") + ": " + username);
        JTextField usernameField = new JTextField(username);

        // Add language selection ComboBox
        JLabel languageLabel = new JLabel(messages.getString("settings.language"));
        JComboBox<String> languageComboBox = new JComboBox<>(new String[]{"English", "Vietnamese"});
        languageComboBox.addActionListener(e -> {
            String selectedLanguage = (String) languageComboBox.getSelectedItem();
            if (selectedLanguage.equals("Vietnamese")) {
                changeLanguage(new Locale("vi", "VN"));
            } else {
                changeLanguage(Locale.ENGLISH);
            }
        });

        // Add components to settings panel
        settingsPanel.add(themeLabel);
        settingsPanel.add(themeComboBox);
        settingsPanel.add(emailsPerPageLabel);
        settingsPanel.add(emailsPerPageField);
        settingsPanel.add(fontSizeLabel);
        settingsPanel.add(fontSizeComboBox);
        settingsPanel.add(sortOrderLabel);
        settingsPanel.add(sortOrderComboBox);
        settingsPanel.add(notificationsLabel);
        settingsPanel.add(notificationsCheckBox);
        settingsPanel.add(autoRefreshLabel);
        settingsPanel.add(autoRefreshCheckBox);
        settingsPanel.add(usernameLabel);
        settingsPanel.add(usernameField);
        settingsPanel.add(languageLabel);
        settingsPanel.add(languageComboBox);

        // Save Button with an icon
        JButton saveButton = new JButton(messages.getString("settings.save"));
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
                JOptionPane.showMessageDialog(parentView, messages.getString("settings.invalidEmailsPerPage"), messages.getString("settings.invalidInput"),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Apply changes
            applyTheme(selectedTheme);
            updateEmailsPerPage(emailsPerPageValue);
            updateFontSize(selectedFontSize);
            updateSortOrder(selectedSortOrder);
            updateNotifications(notificationsEnabled);
            updateAutoRefresh(autoRefreshEnabled);
            updateUsername(newUsername);

            dispose();
        });

        // Reset Button
        JButton resetButton = new JButton(messages.getString("settings.reset"));
        resetButton.setPreferredSize(new Dimension(150, 40));
        resetButton.addActionListener(e -> {
            themeComboBox.setSelectedIndex(0); // Light theme
            fontSizeComboBox.setSelectedIndex(1); // Medium font
            sortOrderComboBox.setSelectedIndex(0); // Date sort
            notificationsCheckBox.setSelected(true);
            autoRefreshCheckBox.setSelected(false);
            emailsPerPageField.setText("20");
            usernameField.setText(username);
        });

        // Logout Button
        JButton logoutButton = new JButton(messages.getString("settings.logout"));
        logoutButton.setPreferredSize(new Dimension(120, 40));
        logoutButton.addActionListener(new ActionListener() {
            private ServerDAO serverDAO;

			@Override
            public void actionPerformed(ActionEvent e) {
                parentView.showLoginScreen(serverDAO);
                dispose();
            }
        });

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(saveButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(logoutButton);

        // Add components to the dialog
        add(settingsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // Change language
    private void changeLanguage(Locale newLocale) {
        this.currentLocale = newLocale;
        this.messages = ResourceBundle.getBundle("messages", currentLocale);  // Load new language
        // Rebuild the dialog UI with new language and retain current values
        initializeDialog();  // Ensure data is retained on language change
    }

    private void applyTheme(String selectedTheme) {
        if ("Dark".equalsIgnoreCase(selectedTheme)) {
            parentView.getContentPane().setBackground(Color.DARK_GRAY);
            parentView.updateStatusLabel(messages.getString("settings.darkThemeApplied"));
        } else if ("Auto".equalsIgnoreCase(selectedTheme)) {
            applyAutoTheme();
        } else {
            parentView.getContentPane().setBackground(Color.WHITE);
            parentView.updateStatusLabel(messages.getString("settings.lightThemeApplied"));
        }
        SwingUtilities.updateComponentTreeUI(parentView);
    }

    private void applyAutoTheme() {
        // Set theme automatically based on the time of day
        LocalTime currentTime = LocalTime.now();
        if (currentTime.isBefore(LocalTime.NOON)) {
            applyTheme("Light");
        } else {
            applyTheme("Dark");
        }
    }

    private void updateEmailsPerPage(int emailsPerPageValue) {
        parentView.setEmailsPerPage(emailsPerPageValue);
        parentView.loadEmails(1);
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

    private void updateSortOrder(String sortOrder) {
        parentView.setSortOrder(sortOrder);
        parentView.loadEmails(1);
    }

    private void updateNotifications(boolean notificationsEnabled) {
        parentView.setNotificationsEnabled(notificationsEnabled);
        if (notificationsEnabled) {
            parentView.updateStatusLabel(messages.getString("settings.notificationsEnabled"));
        } else {
            parentView.updateStatusLabel(messages.getString("settings.notificationsDisabled"));
        }
    }

    private void updateAutoRefresh(boolean autoRefreshEnabled) {
        parentView.setAutoRefreshEnabled(autoRefreshEnabled);
        if (autoRefreshEnabled) {
            parentView.updateStatusLabel(messages.getString("settings.autoRefreshEnabled"));
        } else {
            parentView.updateStatusLabel(messages.getString("settings.autoRefreshDisabled"));
        }
    }

    private void updateUsername(String newUsername) {
        parentView.setUsername(newUsername);
        parentView.updateStatusLabel(messages.getString("settings.usernameUpdated") + " " + newUsername);
    }
}
