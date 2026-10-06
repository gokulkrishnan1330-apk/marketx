package com.marketx.marketx;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class user {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String phone;

    private String password;

    private String role;


    // Empty constructor
    public user() {
    }


    // Constructor
    public user(String name,
                String email,
                String phone,
                String password,
                String role) {

        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
    }


    // ID
    public Long getId() {
        return id;
    }


    // Name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    // Email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    // Phone
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    // Password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    // Role
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}