package client;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import com.toedter.calendar.JDateChooser;
import service.EmailSenderService;

public class SendEmailPanel extends JPanel {

    private JTextField receiverField;
    private JTextField subjectField;
    private JTextArea contentArea;
    private JLabel fileNameLabel;
    private EmailSenderService emailSenderService;
    private JButton scheduleButton;
    private JPanel schedulePanel;

    public SendEmailPanel(MailClientView parent) {
        MailClient client = parent.getClient();
        String userEmail = parent.getUserEmail();
        this.emailSenderService = new EmailSenderService(client, userEmail);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Input fields for receiver and subject
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createTitledBorder("Email Details"));

        receiverField = new JTextField(20);
        subjectField = new JTextField(20);

        inputPanel.add(new JLabel("Receiver Email:"));
        inputPanel.add(receiverField);
        inputPanel.add(new JLabel("Subject:"));
        inputPanel.add(subjectField);

        // Attachment panel
        JPanel attachmentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        attachmentPanel.setBackground(Color.WHITE);
        attachmentPanel.setBorder(BorderFactory.createTitledBorder("Attachment"));

        JButton attachButton = new JButton("Choose File");
        fileNameLabel = new JLabel("No file chosen");
        attachButton.addActionListener(e -> chooseFilesToAttach());

        attachmentPanel.add(new JLabel("Attach File:"));
        attachmentPanel.add(attachButton);
        attachmentPanel.add(fileNameLabel);

        // Content area
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createTitledBorder("Email Content"));

        contentArea = new JTextArea(10, 30);
        contentArea.setWrapStyleWord(true);
        contentArea.setLineWrap(true);
        contentPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton sendButton = new JButton("📧 Send Email");
        sendButton.addActionListener(e -> sendEmail());

        JButton clearButton = new JButton("🧹 Clear Fields");
        clearButton.addActionListener(e -> clearFields());

        // Schedule Button to show/hide the scheduling panel
        scheduleButton = new JButton("⏰ Schedule Send");
        scheduleButton.addActionListener(e -> toggleSchedulePanelVisibility());

        buttonPanel.add(sendButton);
        buttonPanel.add(scheduleButton);
        buttonPanel.add(clearButton);

        // Schedule Panel for date and time selection
        schedulePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        schedulePanel.setBackground(Color.WHITE);
        schedulePanel.setVisible(false); // Hide by default

        JLabel scheduleLabel = new JLabel("Select Date and Time:");
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm:ss"));

        schedulePanel.add(scheduleLabel);
        schedulePanel.add(dateChooser);
        schedulePanel.add(timeSpinner);

        // Add components to the main panel
        add(inputPanel);
        add(Box.createVerticalStrut(10));
        add(attachmentPanel);
        add(Box.createVerticalStrut(10));
        add(contentPanel);
        add(Box.createVerticalStrut(10));
        add(schedulePanel); // Add schedule panel below content
        add(Box.createVerticalStrut(10));
        add(buttonPanel);
    }

    private void toggleSchedulePanelVisibility() {
        // Show or hide the schedule panel when the button is clicked
        boolean isVisible = !schedulePanel.isVisible();
        schedulePanel.setVisible(isVisible);
        scheduleButton.setText(isVisible ? "❌ Cancel Schedule" : "⏰ Schedule Send"); // Change button text based on state
        revalidate(); // Re-layout the panel
        repaint();
    }
	private void sendEmail() {
        String receiver = receiverField.getText();
        String subject = subjectField.getText();
        String content = contentArea.getText();

        // Basic validation checks
        if (receiver.isEmpty() || subject.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String response = emailSenderService.sendEmail(receiver, subject, content);
            JOptionPane.showMessageDialog(this, response, "Email Sent", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Email", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, " An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
	 private void scheduleEmail(String receiver, String subject, String content, Date scheduledTime) {
	        // Logic to schedule email
	        String scheduledDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(scheduledTime);
	        JOptionPane.showMessageDialog(this, "Email scheduled for: " + scheduledDateTime, "Scheduled", JOptionPane.INFORMATION_MESSAGE);

	        // You can add scheduling logic here if your email service supports scheduling
	        // For example, saving the scheduled time and email content, then sending it later
	    }

	 private void chooseFilesToAttach() {
		    JFileChooser fileChooser = new JFileChooser();
		    fileChooser.setMultiSelectionEnabled(true);  // Cho phép chọn nhiều file
		    int returnValue = fileChooser.showOpenDialog(null);
		    if (returnValue == JFileChooser.APPROVE_OPTION) {
		        // Lấy các file đã chọn
		        File[] selectedFiles = fileChooser.getSelectedFiles();
		        if (selectedFiles.length > 0) {
		            // Hiển thị tên các tệp đã chọn
		            StringBuilder fileNames = new StringBuilder("Selected: ");
		            for (File file : selectedFiles) {
		                fileNames.append(file.getName()).append(" ");
		            }
		            fileNameLabel.setText(fileNames.toString());
		        }
		    } else {
		        fileNameLabel.setText("No files chosen");
		    }
		}


	    private void clearFields() {
	        receiverField.setText("");
	        subjectField.setText("");
	        contentArea.setText("");
	        fileNameLabel.setText("No file chosen");
	    }

	    public void setInitialValues(String sender, String subject, String quotedContent) {
	        // Đặt giá trị cho trường "Người nhận"
	        receiverField.setText(sender);

	        // Đặt giá trị cho trường "Chủ đề"
	        subjectField.setText(subject);

	        // Đặt giá trị cho phần "Nội dung"
	        contentArea.setText(quotedContent);
	    }


	}