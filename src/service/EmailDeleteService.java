package service;

import client.MailClient;
import client.MailClientView;
import javax.swing.*;
import javax.swing.JTable;

public class EmailDeleteService {
    private MailClientView mailClientView;
    private MailClient client;

    public EmailDeleteService(MailClient client,MailClientView mailClientView) {
        this.mailClientView = mailClientView;
        this.client = client;
    }

    public String deleteEmail(String userEmail, String emailId) {
        try {
            // Send request to delete the email from the server
            String response = client.sendRequest("DELETE_EMAIL:" + userEmail + ":" + emailId);
            return response; // Return the response to be handled by the controller
        } catch (Exception ex) {
            mailClientView.showNotification("An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return null; // Return null to indicate failure
        }
    }
}
