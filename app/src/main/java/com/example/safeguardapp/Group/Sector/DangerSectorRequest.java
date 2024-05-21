package com.example.safeguardapp.Group.Sector;

public class DangerSectorRequest {
    private double xOfPointA, yOfPointA, xOfPointB, yOfPointB, xOfPointC, yOfPointC, xOfPointD, yOfPointD;
    private String childName;

    public DangerSectorRequest(double xOfPointA, double yOfPointA, double xOfPointB, double yOfPointB,
                             double xOfPointC, double yOfPointC, double xOfPointD, double yOfPointD, String childName){
        this.xOfPointA = xOfPointA;
        this.yOfPointA = yOfPointA;
        this.xOfPointB = xOfPointB;
        this.yOfPointB = yOfPointB;
        this.xOfPointC = xOfPointC;
        this.yOfPointC = yOfPointC;
        this.xOfPointD = xOfPointD;
        this.yOfPointD = yOfPointD;
        this.childName = childName;
    }
}
