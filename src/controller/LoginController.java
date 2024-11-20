//package controller;
//
//import client.LoginView;
//import service.LoginService;
//
//public class LoginController {
//
//    private LoginService loginService;
//    private LoginView loginView;
//
//    public LoginController(LoginView loginView, LoginService loginService) {
//        this.loginView = loginView;
//        this.loginService = loginService;
//    }
//
//    // Handle login logic
//    public void handleLogin(String email, String password) {
//        loginView.setStatusMessage("Logging in...");
//        
//        String result = loginService.authenticateUser(email, password);
//        
//        if ("successful".equals(result)) {
//            loginView.openMailClientView(null, result, null, null);
//        } else {
//            loginView.setStatusMessage(result); // Display failure message
//        }
//    }
//
//    // Handle registration (you can expand this later)
//    public void handleRegister() {
//        loginView.openRegisterView();
//    }
//}
