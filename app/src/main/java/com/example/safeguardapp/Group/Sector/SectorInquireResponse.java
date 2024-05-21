package com.example.safeguardapp.Group.Sector;


import java.util.Map;
public class SectorInquireResponse {
    private Map<String, SectorDetails> sectors;

    // Getters and setters
    public Map<String, SectorDetails> getSectors() {
        return sectors;
    }

    public void setSectors(Map<String, SectorDetails> sectors) {
        this.sectors = sectors;
    }
}
