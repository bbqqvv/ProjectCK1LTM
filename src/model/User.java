package model;

import java.sql.Timestamp;

public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private String ipAddress; // Thay đổi từ ipId sang ipAddress
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private boolean isLogin;  // Thêm trường isLogin

    // Constructor with isLogin parameter
    public User(int id, String username, String password, String email, String ipAddress, Timestamp createdAt, Timestamp updatedAt, boolean isLogin) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.ipAddress = ipAddress; // Khởi tạo địa chỉ IP
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isLogin = isLogin;  // Khởi tạo trạng thái đăng nhập
    }
    
    public User(int id, String username, String password, String email, boolean isLogin) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.isLogin = isLogin;
    }

    // Default constructor
    public User() {

    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Getter and Setter for isLogin
    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean isLogin) {
        this.isLogin = isLogin;
    }
}
