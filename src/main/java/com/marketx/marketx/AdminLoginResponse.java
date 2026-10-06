package com.marketx.marketx;

public class AdminLoginResponse {

    private Long id;
    private String username;

    public AdminLoginResponse() {
    }

    public AdminLoginResponse(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}