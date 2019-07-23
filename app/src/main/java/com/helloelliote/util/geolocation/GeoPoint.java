package com.helloelliote.util.geolocation;

import androidx.annotation.NonNull;

import lombok.Getter;

@Getter
public class GeoPoint {
    public double x;
    public double y;
    public double z;

    GeoPoint() {
        super();
    }

    public GeoPoint(double x, double y) {
        super();
        this.x = x;
        this.y = y;
        this.z = 0;
    }

    public GeoPoint(double x, double y, double z) {
        super();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @NonNull
    @Override
    public String toString() {
        return "GeoPoint{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
    }
}
