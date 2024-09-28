package com.ecommerce.project.security.request;

import java.util.List;

public class LoginResponse {

    private Long id;
    private String jwt;
    private String username;
    private List<String> roles;

    public LoginResponse(Long id,String jwt, String username, List<String> roles) {
        this.jwt = jwt;
        this.username = username;
        this.roles = roles;
        this.id=id;
    }

    public LoginResponse(Long id, String username, List<String> roles) {
        this.username = username;
        this.roles = roles;
        this.id=id;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
