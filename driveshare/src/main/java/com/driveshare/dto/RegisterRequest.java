package com.driveshare.dto;

public class RegisterRequest {

    private String username;
    private String email;
    private String password;
    private String securityAnswer1;
    private String securityAnswer2;
    private String securityAnswer3;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getSecurityAnswer1() { return securityAnswer1; }
    public void setSecurityAnswer1(String securityAnswer1) { this.securityAnswer1 = securityAnswer1; }

    public String getSecurityAnswer2() { return securityAnswer2; }
    public void setSecurityAnswer2(String securityAnswer2) { this.securityAnswer2 = securityAnswer2; }

    public String getSecurityAnswer3() { return securityAnswer3; }
    public void setSecurityAnswer3(String securityAnswer3) { this.securityAnswer3 = securityAnswer3; }
}
