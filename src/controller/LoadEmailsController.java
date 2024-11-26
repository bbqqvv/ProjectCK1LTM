package controller;

import client.LoadEmailsPanel;
import client.MailClient;
import client.MailClientView;
import service.EmailDeleteService;
import service.EmailLoaderService;
import model.Mail;

import javax.swing.*;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller quản lý việc tải và xử lý các email.
 */
public class LoadEmailsController {
    private final LoadEmailsPanel loadEmailsPanel;
    private final EmailLoaderService emailLoaderService;
    private final EmailDeleteService emailDeleteService;
    private final String userEmail;
    private int currentPage = 1;
    private final int emailsPerPage = 10;
    private final List<Mail> allEmails;
    public LoadEmailsController(LoadEmailsPanel loadEmailsPanel, MailClient client, String userEmail) {
        this.loadEmailsPanel = loadEmailsPanel;
        this.userEmail = userEmail;
        this.emailLoaderService = new EmailLoaderService(client, userEmail);
        this.emailDeleteService = new EmailDeleteService(client); // Chỉ cần MailClient
        this.allEmails = new ArrayList<>();
    }

    /**
     * Tải email cho một trang nhất định.
     */
    public void loadEmails(int page) {
        SwingWorker<List<Mail>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Mail> doInBackground() {
                try {
                    return emailLoaderService.loadEmails(page, emailsPerPage);
                } catch (Exception e) {
                    e.printStackTrace();
                    loadEmailsPanel.showNotification("Lỗi khi tải email: " + e.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    List<Mail> mails = get();
                    if (mails != null) {
                        allEmails.clear();
                        allEmails.addAll(mails);
                        loadEmailsPanel.getEmailList().clear();
                        loadEmailsPanel.getEmailList().addAll(mails);
                        updateTableData(mails);
                        loadEmailsPanel.updateStatusLabel("Hiển thị trang " + page);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
        worker.execute();
    }

    /**
     * Cập nhật dữ liệu vào bảng.
     */
// When emails are loaded, make sure each Mail object includes the content.
    private void updateTableData(List<Mail> mails) {
        allEmails.clear();
        allEmails.addAll(mails);
        loadEmailsPanel.getEmailList().clear();
        loadEmailsPanel.getEmailList().addAll(mails);

        DefaultTableModel model = loadEmailsPanel.getEmailTableModel();
        model.setRowCount(0);
        for (Mail mail : mails) {
            model.addRow(new Object[]{
                    mail.getId(),
                    mail.getSender(),
                    mail.getSubject(),
                    mail.getSentDate()
            });
        }
    }



    /**
     * Xử lý phân trang.
     */
    public void handlePagination(boolean next) {
        currentPage = next ? currentPage + 1 : Math.max(1, currentPage - 1);
        loadEmails(currentPage);
    }

    /**
     * Xử lý tìm kiếm email theo query.
     */
    public void handleSearch(String query) {
        List<Mail> filteredEmails = filterEmails(query);
        updateTableData(filteredEmails);
        loadEmailsPanel.updateStatusLabel("Đã tìm thấy " + filteredEmails.size() + " email.");
    }

    /**
     * Lọc email theo query.
     */
    private List<Mail> filterEmails(String query) {
        List<Mail> filtered = new ArrayList<>();
        for (Mail mail : allEmails) {
            if (mail.getSubject().toLowerCase().contains(query.toLowerCase())
                    || mail.getSender().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(mail);
            }
        }
        return filtered;
    }

    /**
     * Xử lý xóa email.
     */
    public void handleDeleteEmail() {
        int selectedRow = loadEmailsPanel.getSelectedRow(); // Lấy hàng đang được chọn trong bảng email
        if (selectedRow >= 0) {
            String emailId = loadEmailsPanel.getEmailIdAtRow(selectedRow); // Lấy ID của email cần xóa
            int confirm = JOptionPane.showConfirmDialog(loadEmailsPanel.getMailClientView(),
                    "Bạn có chắc muốn xóa email này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        try {
                            // Gọi EmailDeleteService để xóa email
                            String response = emailDeleteService.deleteEmail(userEmail, emailId);

                            // Kiểm tra phản hồi từ server
                            if (response != null && response.contains("successfully")) {
                                loadEmailsPanel.showNotification(response, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                loadEmailsPanel.showNotification("Xóa email thất bại: " + response, "Lỗi", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception e) {
                            handleError("Lỗi khi xóa email", e);
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        loadEmails(currentPage); // Tải lại danh sách email sau khi xóa
                    }
                };
                worker.execute();
            }
        } else {
            loadEmailsPanel.showNotification("Vui lòng chọn email để xóa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
    }
    private void handleError(String message, Exception e) {
        e.printStackTrace();
        loadEmailsPanel.showNotification(message + ": " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Lấy danh sách tất cả các email đã tải.
     *
     * @return Danh sách tất cả các email.
     */
    public List<Mail> getAllEmails() {
        return new ArrayList<>(allEmails); // Trả về một bản sao danh sách để tránh thay đổi bên ngoài
    }

    public void setEmailsPerPage(int emailsPerPage2) {
        // TODO Auto-generated method stub

    }

    public void setSortOrder(String sortOrder) {
        // TODO Auto-generated method stub

    }

    public void setNotificationsEnabled(boolean enabled) {
        // TODO Auto-generated method stub

    }

    public void handleReplyEmail() {
        // TODO Auto-generated method stub

    }

}


