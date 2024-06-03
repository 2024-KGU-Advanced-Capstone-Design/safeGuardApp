package com.example.safeguardapp.data.model;

import java.util.UUID;

public class OtherGroup {
    private final String uuid = UUID.randomUUID().toString();
    private String name;
    private String id;

    public OtherGroup() {

    }

    public OtherGroup(String name, String id) {
        this.name = name;
        this.id = id;
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
}
