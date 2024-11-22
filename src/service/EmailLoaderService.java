package service;

import client.MailClient;
import model.Mail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service xử lý việc tải email từ server.
 */
public class EmailLoaderService {
    private final MailClient client;
    private final String userEmail;

    public EmailLoaderService(MailClient client, String userEmail) {
        this.client = client;
        this.userEmail = userEmail;
    }

    /**
     * Tải email từ server.
     */
    public List<Mail> loadEmails(int page, int emailsPerPage) throws Exception {
        if (page <= 0) throw new IllegalArgumentException("Page number must be greater than 0.");
        String request = "LOAD_EMAILS:" + userEmail + ":" + page + ":" + emailsPerPage;
        String response = client.sendRequest(request);
        if (response == null || response.isEmpty()) throw new Exception("Không có email nào hoặc server không phản hồi.");

        List<Mail> emailList = new ArrayList<>();
        String regex = "ID: (\\d+), Sender: ([^,]+), Receiver: ([^,]+), Subject: ([^,]+), Content: ([^,]+), Sent Date: ([^,]+), Is Sent: (.+)";
        Pattern pattern = Pattern.compile(regex);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (String email : response.split("\n")) {
            if (email.trim().isEmpty()) continue;
            Matcher matcher = pattern.matcher(email);
            if (matcher.find()) {
                int id = Integer.parseInt(matcher.group(1));
                String sender = matcher.group(2);
                String subject = matcher.group(4);
                String content = matcher.group(5);
                Date sentDate = dateFormat.parse(matcher.group(6));
                Mail mail = new Mail(id, sender, matcher.group(3), subject, content, sentDate, Boolean.parseBoolean(matcher.group(7)));
                emailList.add(mail);
            }
        }
        return emailList;
    }


}
