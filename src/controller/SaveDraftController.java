package controller;

import dao.SaveDarftDao;
import model.SaveDraft;

import javax.swing.*;
import java.sql.Connection;

public class SaveDraftController {
    private SaveDarftDao saveDarftDao;

    // Constructor, nơi bạn truyền Connection từ bên ngoài
    public SaveDraftController(Connection connection) {
        this.saveDarftDao = new SaveDarftDao(connection);
    }

    public void saveDraft(String receiver, String subject, String content, int userId) {
        try {
            if (receiver.isEmpty() || subject.isEmpty() || content.isEmpty()) {
                throw new IllegalArgumentException("Please fill in all fields.");
            }

            // Tạo đối tượng Save và lưu vào CSDL
            SaveDraft saveDraft = new SaveDraft(userId, receiver, subject, content);  // Thêm userId vào constructor
            int result = saveDarftDao.saveEmail(saveDraft);

            if (result > 0) {
                JOptionPane.showMessageDialog(null, "Draft saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Failed to save draft", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
