-- Sử dụng cơ sở dữ liệu mail_app
USE mail_app;

-- Tạo bảng IPAddress để lưu địa chỉ IP
CREATE TABLE IPAddress (
    id INT PRIMARY KEY AUTO_INCREMENT,
    ip_address VARCHAR(45) NOT NULL -- Dùng VARCHAR(45) để hỗ trợ cả IPv4 và IPv6
);

-- Tạo bảng Users để quản lý người dùng
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    ip_id INT, -- Khóa ngoại lưu địa chỉ IP
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT FK_Users_IPAddress FOREIGN KEY (ip_id) REFERENCES IPAddress(id)
);activity_logsactivity_logs

-- Tạo bảng Mails để quản lý các email
CREATE TABLE mails (
    id INT AUTO_INCREMENT PRIMARY KEY,     -- ID duy nhất cho mỗi email
    sender VARCHAR(255) NOT NULL,          -- Địa chỉ email người gửi
    receiver VARCHAR(255) NOT NULL,        -- Địa chỉ email người nhận
    subject VARCHAR(255) NOT NULL,         -- Tiêu đề email
    content TEXT NOT NULL,                 -- Nội dung email
    sent_date DATETIME NOT NULL,           -- Ngày giờ gửi email
    is_sent BOOLEAN NOT NULL DEFAULT FALSE -- Trạng thái đã gửi
);




-- Tạo bảng Attachments để quản lý tệp đính kèm
CREATE TABLE attachments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    mail_id INT NOT NULL,                  -- Email liên quan
    file_name VARCHAR(255) NOT NULL,       -- Tên tệp đính kèm
    file_path VARCHAR(255) NOT NULL,       -- Đường dẫn tệp trên hệ thống
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Attachment_Mail FOREIGN KEY (mail_id) REFERENCES mails(id)
);



-- Tạo bảng Activity_Logs để quản lý nhật ký hoạt động
CREATE TABLE activity_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    mail_id INT NOT NULL,
    action VARCHAR(255) NOT NULL,          -- Ví dụ: 'sent', 'deleted', 'moved to spam'
    action_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Log_User FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT FK_Log_Mail FOREIGN KEY (mail_id) REFERENCES mails(id)
);


-- Tạo bảng Spam để quản lý các email bị đánh dấu là thư rác
CREATE TABLE spam (
    id INT PRIMARY KEY AUTO_INCREMENT,
    mail_id INT NOT NULL,
    user_id INT NOT NULL,
    marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Spam_Mail FOREIGN KEY (mail_id) REFERENCES mails(id),
    CONSTRAINT FK_Spam_User FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Tạo bảng Archives để quản lý các email đã lưu trữ
CREATE TABLE archives (
    id INT PRIMARY KEY AUTO_INCREMENT,
    mail_id INT NOT NULL,
    user_id INT NOT NULL,
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Archive_Mail FOREIGN KEY (mail_id) REFERENCES mails(id),
    CONSTRAINT FK_Archive_User FOREIGN KEY (user_id) REFERENCES users(id)
);
