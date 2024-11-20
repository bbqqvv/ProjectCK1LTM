//package service;
//
//import client.MailClient;
//import client.MailClientView;
//import client.SendEmailPanel;
//import javax.swing.JOptionPane;
//import javax.swing.JTable;
//
//public class EmailReplyService {
//    private MailClientView mailClientView;
//    private MailClient client;
//
//    public EmailReplyService(MailClientView mailClientView, MailClient client) {
//        this.mailClientView = mailClientView;
//        this.client = client;
//    }
//
//    // Xử lý trả lời email
//    public void replyEmail(JTable emailTable, String userEmail, int currentPage) {
//        int selectedRow = emailTable.getSelectedRow();
//        if (selectedRow == -1) {
//            mailClientView.showNotification("Select an email to reply to.", "Warning", JOptionPane.WARNING_MESSAGE);
//            return;
//        }
//
//        // Lấy thông tin từ email đã chọn
//        String sender = emailTable.getValueAt(selectedRow, 1).toString();
//        String subject = "Re: " + emailTable.getValueAt(selectedRow, 2).toString();
//        String originalContent = mailClientView.getEmailContents().get(selectedRow);
//        String date = emailTable.getValueAt(selectedRow, 3).toString();
//
//        // Xây dựng nội dung trả lời
//        String quotedContent = buildQuotedContent(sender, date, originalContent);
//
//        // Tạo một panel mới để trả lời email
//        SendEmailPanel replyPanel = new SendEmailPanel(mailClientView);
//        replyPanel.setInitialValues(sender, subject, quotedContent);
//
//        // Hiển thị panel trả lời email
//        mailClientView.getMainPanel().add(replyPanel, "ReplyEmail");
//        mailClientView.switchPanel("ReplyEmail");
//    }
//
//    private String buildQuotedContent(String sender, String date, String originalContent) {
//        StringBuilder quotedContent = new StringBuilder();
//        quotedContent.append("<br><br>--- Original Message ---<br>");
//        quotedContent.append("<strong>From:</strong> ").append(sender).append("<br>");
//        quotedContent.append("<strong>Sent:</strong> ").append(date).append("<br>");
//        quotedContent.append("<strong>Content:</strong><br>");
//        quotedContent.append(originalContent.replaceAll("(\r\n|\n)", "<br>"));
//        return quotedContent.toString();
//    }
//}
