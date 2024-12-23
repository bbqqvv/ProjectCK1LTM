package client;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import model.Mail;
import dao.SaveDarftDao;
import model.SaveDraft;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoadEmailDraftPanel extends JPanel {
    private JTable emailTable;
    private DefaultTableModel emailTableModel;
    private MailClientView mailClientView;
    private JTextPane emailDetailsArea;
    private List<Mail> emailList = new ArrayList<>();
    private SaveDarftDao saveDarftDao;

    public LoadEmailDraftPanel(MailClientView parent, SaveDarftDao saveDarftDao) {
        this.mailClientView = parent;
        this.saveDarftDao = saveDarftDao;
        createComponents();
        loadSavedEmails();
    }

    private void createComponents() {
        // Tạo panel tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("🔍 Tìm kiếm");

        searchPanel.add(new JLabel("Tìm kiếm theo Subject hoặc Sender:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        // Tạo bảng email
        emailTableModel = new DefaultTableModel(new Object[]{"Receiver", "Subject", "Content"}, 0);
        emailTable = new JTable(emailTableModel);
        emailTable.setRowHeight(40); // Đặt chiều cao dòng
        emailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailTable.setDefaultEditor(Object.class, null);
        customizeTableAppearance();

        // Thêm bảng vào giao diện
        JScrollPane emailScrollPane = new JScrollPane(emailTable);
        add(emailScrollPane, BorderLayout.CENTER);

        // Tạo vùng chi tiết và phân chia giao diện
        emailDetailsArea = new JTextPane();
        JScrollPane detailsScrollPane = new JScrollPane(emailDetailsArea);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, emailScrollPane, detailsScrollPane);
        splitPane.setResizeWeight(0.7);
        add(splitPane, BorderLayout.CENTER);

        // Tạo các nút phân trang
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton prevPageButton = new JButton("◀ Trang trước");
        JButton nextPageButton = new JButton("Trang sau ▶");
        JLabel pageLabel = new JLabel("Trang: 1");

        paginationPanel.add(prevPageButton);
        paginationPanel.add(pageLabel);
        paginationPanel.add(nextPageButton);
        add(paginationPanel, BorderLayout.SOUTH);

        // Lắng nghe sự kiện chọn dòng trong bảng
        emailTable.getSelectionModel().addListSelectionListener(e -> showEmailDetails());

        // Thêm menu chuột phải
        addRightClickMenu();
    }

    private void addRightClickMenu() {
        // Tạo popup menu cho chuột phải
        JPopupMenu popupMenu = new JPopupMenu();

        // Thêm mục "Xóa"
        JMenuItem deleteMenuItem = new JMenuItem("Delete");
        deleteMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Xử lý xóa email
            }
        });

        // Thêm mục "Trả lời"
        JMenuItem replyMenuItem = new JMenuItem("Reply");
        replyMenuItem.addActionListener(e -> {
            try {
                handleReplyEmail();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        JMenuItem chatMenuItem = new JMenuItem("Chat");
        popupMenu.add(deleteMenuItem);
        popupMenu.add(replyMenuItem);
        popupMenu.add(chatMenuItem);
        // Lắng nghe sự kiện chuột phải
        emailTable.setComponentPopupMenu(popupMenu);
    }

    private void handleReplyEmail() throws SQLException {
        int selectedRow = emailTable.getSelectedRow();

        if (selectedRow < 0 || selectedRow >= emailList.size()) {
            showNotification("Vui lòng chọn email để trả lời.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Lấy email được chọn
        Mail selectedMail = emailList.get(selectedRow);

        // Gọi view để hiển thị giao diện trả lời email
        mailClientView.showReplyEmailPanel(selectedMail);
    }

    private void showEmailDetails() {
        int selectedRow = emailTable.getSelectedRow();

        if (selectedRow < 0 || selectedRow >= emailList.size()) {
            emailDetailsArea.setText("Please select an email to view its details.");
            return;
        }

        Mail selectedMail = emailList.get(selectedRow);

        if (selectedMail == null) {
            emailDetailsArea.setText("No details available for the selected email.");
            return;
        }

        String subject = selectedMail.getSubject();
        String sender = selectedMail.getReceiver();
        String content = selectedMail.getContent();
        if (content == null || content.isEmpty()) {
            content = "No content available for this email.";
        }

        String emailDetailsHTML = "<html><head><style>"
                + "body { font-family: Arial, sans-serif; font-size: 14px; }"
                + ".email-container { margin: 10px; padding: 10px; border: 1px solid #ddd; }"
                + ".header { font-size: 18px; font-weight: bold; margin-bottom: 10px; }"
                + ".email-info { margin-bottom: 5px; }"
                + ".content { white-space: pre-wrap; word-wrap: break-word; }"
                + "</style></head><body>"
                + "<div class='email-container'>"
                + "<div class='header'>Subject: <span class='subject'>" + subject + "</span></div>"
                + "<div class='email-info'>From: <b>" + sender + "</b></div>"
                + "<div class='content'>" + content + "</div>"
                + "</div></body></html>";

        emailDetailsArea.setText(emailDetailsHTML);
        emailDetailsArea.setCaretPosition(0);
    }

    private void loadSavedEmails() {
        String userEmail = mailClientView.getUserEmail(); // Lấy email của người dùng hiện tại
        List<SaveDraft> savedEmails = saveDarftDao.getAllMailsForUser(userEmail); // Truy vấn email của người dùng hiện tại từ DAO
        updateEmailTable(savedEmails);  // Cập nhật bảng với các email đã lưu
    }


    private void updateEmailTable(List<SaveDraft> emails) {
        emailTableModel.setRowCount(0); // Xóa tất cả các dòng hiện tại

        for (SaveDraft mail : emails) {
            emailTableModel.addRow(new Object[]{mail.getReceiver(), mail.getSubject(), mail.getContent()});
        }
    }

    public void showNotification(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    private void customizeTableAppearance() {
        JTableHeader header = emailTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(230, 230, 250)); // Light purple for header
        header.setForeground(Color.BLACK);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < emailTable.getColumnCount(); i++) {
            emailTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        emailTable.setSelectionBackground(new Color(184, 207, 229));
        emailTable.setSelectionForeground(Color.BLACK);
    }
}
