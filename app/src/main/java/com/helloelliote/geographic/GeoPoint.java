package com.helloelliote.geographic;

import android.support.annotation.NonNull;

import lombok.Getter;

@Getter
public class GeoPoint {
    double x;
    double y;
    double z;

    /**
     *
     */
    GeoPoint() {
        super();
    }

    /**
     * @param x
     * @param y
     */
    public GeoPoint(double x, double y) {
        super();
        this.x = x;
        this.y = y;
        this.z = 0;
    }

    /**
     * @param x
     * @param y
     * @param y
     */
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
