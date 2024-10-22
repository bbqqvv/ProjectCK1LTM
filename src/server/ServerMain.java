package server;

public class ServerMain {
    public static void main(String[] args) {
        MailServer server = new MailServer();
        ServerView view = new ServerView();
        server.setView(view);
        server.start();
    }
}
