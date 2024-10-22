package client;

import javax.swing.JOptionPane;

public class ClientMain {
    public static void main(String[] args) {
        try {
            String serverAddress = JOptionPane.showInputDialog("Enter server IP:");
            MailClient client = new MailClient(serverAddress, 4445);
            LoginView loginView = new LoginView(client);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
