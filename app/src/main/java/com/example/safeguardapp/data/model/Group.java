package com.example.safeguardapp.data.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Group {
    private final String uuid = UUID.randomUUID().toString();
    private String name;
    private String id;
    private String password;
    private List<String> aide = new ArrayList<>();

    public Group() {
    }

    public Group(String name, String id, String password) {
        this.name = name;
        this.id = id;
        this.password = password;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getAide() {
        return aide;
    }

    public void setAide(List<String> aide) {
        this.aide = aide;
    }
}
