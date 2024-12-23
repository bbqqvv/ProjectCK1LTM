package service;

import client.MailClient;
import model.Mail;

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
     *
     * @param page          Trang cần tải.
     * @param emailsPerPage Số email trên mỗi trang.
     * @return Danh sách các email.
     * @throws Exception Nếu có lỗi xảy ra khi tải email.
     */
    public List<Mail> loadEmails(int page, int emailsPerPage) throws Exception {
        validatePageNumber(page); // Kiểm tra số trang hợp lệ

        String request = buildRequest(page, emailsPerPage); // Tạo yêu cầu gửi đến server
        String response = client.sendRequest(request); // Sử dụng client để gửi yêu cầu

        if (response == null || response.isEmpty()) {
            throw new Exception("Không có email nào hoặc server không phản hồi.");
        }

        return parseEmailResponse(response); // Phân tích và trả về danh sách email
    }

    /**
     * Kiểm tra số trang hợp lệ.
     *
     * @param page Số trang.
     * @throws IllegalArgumentException Nếu số trang không hợp lệ.
     */
    private void validatePageNumber(int page) throws IllegalArgumentException {
        if (page <= 0) {
            throw new IllegalArgumentException("Page number must be greater than 0.");
        }
    }

    /**
     * Tạo yêu cầu tải email từ server.
     *
     * @param page          Trang cần tải.
     * @param emailsPerPage Số email trên mỗi trang.
     * @return Yêu cầu để gửi đến server.
     */
    private String buildRequest(int page, int emailsPerPage) {
        return "LOAD_EMAILS:" + userEmail + ":" + page + ":" + emailsPerPage;
    }

    /**
     * Phân tích phản hồi từ server và chuyển đổi thành danh sách email.
     *
     * @param response Phản hồi từ server.
     * @return Danh sách các email.
     * @throws Exception Nếu có lỗi xảy ra trong quá trình phân tích.
     */
    private List<Mail> parseEmailResponse(String response) throws Exception {
        List<Mail> emailList = new ArrayList<>();
        String regex = "ID: (\\d+), Sender: ([^,]+), Receiver: ([^,]+), Subject: ([^,]+), Content: ([^,]+), Sent Date: ([^,]+), Is Sent: (.+)";
        Pattern pattern = Pattern.compile(regex);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (String email : response.split("\n")) {
            if (email.trim().isEmpty()) continue;
            Matcher matcher = pattern.matcher(email);
            if (matcher.find()) {
                try {
                    int id = Integer.parseInt(matcher.group(1));
                    String sender = matcher.group(2);
                    String receiver = matcher.group(3);
                    String subject = matcher.group(4);
                    String content = matcher.group(5);
                    Date sentDate = dateFormat.parse(matcher.group(6));
                    boolean isSent = Boolean.parseBoolean(matcher.group(7));
                    Mail mail = new Mail(id, sender, receiver, subject, content, sentDate, isSent);
                    emailList.add(mail);
                } catch (Exception e) {
                    throw new Exception("Lỗi khi phân tích email: " + email, e);
                }
            }
        }
        return emailList;
    }
}
