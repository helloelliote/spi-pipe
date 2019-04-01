package com.helloelliote.geographic;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import lombok.Getter;

public class GeoTrans {

    @Contract(pure = true)
    private static double D2R(double degree) {
        return degree * Math.PI / 180.0;
    }

    @Contract(pure = true)
    private static double R2D(double radian) {
        return radian * 180.0 / Math.PI;
    }

    @Contract(pure = true)
    private static double e0fn(double x) {
        return 1.0 - 0.25 * x * (1.0 + x / 16.0 * (3.0 + 1.25 * x));
    }

    @Contract(pure = true)
    private static double e1fn(double x) {
        return 0.375 * x * (1.0 + 0.25 * x * (1.0 + 0.46875 * x));
    }

    @Contract(pure = true)
    private static double e2fn(double x) {
        return 0.05859375 * x * x * (1.0 + 0.75 * x);
    }

    @Contract(pure = true)
    private static double e3fn(double x) {
        return x * x * x * (35.0 / 3072.0);
    }

    private static double mlfn(double e0, double e1, double e2, double e3, double phi) {
        return e0 * phi - e1 * Math.sin(2.0 * phi) + e2 * Math.sin(4.0 * phi) - e3 * Math.sin(6.0 * phi);
    }

    private static double asinz(double value) {
        if (Math.abs(value) > 1.0) value = (value > 0 ? 1 : -1);
        return Math.asin(value);
    }

    public static GeoPoint convert(Coordinate srcType, Coordinate dstType, GeoPoint in_pt) {
        GeoPoint tmpPt = new GeoPoint();
        GeoPoint out_pt = new GeoPoint();

        if (srcType == Coordinate.GEO) {
            tmpPt.x = D2R(in_pt.x);
            tmpPt.y = D2R(in_pt.y);
        } else {
            tm2geo(srcType, in_pt, tmpPt);
        }

        if (dstType == Coordinate.GEO) {
            out_pt.x = R2D(tmpPt.x);
            out_pt.y = R2D(tmpPt.y);
        } else {
            geo2tm(dstType, tmpPt, out_pt);
//            out_pt.x = Math.round(out_pt.x);
//            out_pt.y = Math.round(out_pt.y);
        }

        return out_pt;
    }

    private static void geo2tm(Coordinate dstType, GeoPoint in_pt, GeoPoint out_pt) {
        double x, y;

        transform(Coordinate.GEO, dstType, in_pt);
        double delta_lon = in_pt.x - dstType.getArLonCenter();
        double sin_phi = Math.sin(in_pt.y);
        double cos_phi = Math.cos(in_pt.y);

        if (dstType.getInd() != 0) {
            double b = cos_phi * Math.sin(delta_lon);

            if ((Math.abs(Math.abs(b) - 1.0)) < dstType.getEPSLN()) {
//                Log.d("무한대 에러");
//                System.out.println("무한대 에러");
            }
        } else {
            double b = 0;
            x = 0.5 * dstType.getArMajor() * dstType.getArScaleFactor() * Math.log((1.0 + b) / (1.0 - b));
            double con = Math.acos(cos_phi * Math.cos(delta_lon) / Math.sqrt(1.0 - b * b));

            if (in_pt.y < 0) {
                con = con * -1;
                y = dstType.getArMajor() * dstType.getArScaleFactor() * (con - dstType.getArLatCenter());
            }
        }

        double al = cos_phi * delta_lon;
        double als = al * al;
        double c = dstType.getEsp() * cos_phi * cos_phi;
        double tq = Math.tan(in_pt.y);
        double t = tq * tq;
        double con = 1.0 - dstType.getEs() * sin_phi * sin_phi;
        double n = dstType.getArMajor() / Math.sqrt(con);
        double ml = dstType.getArMajor() * mlfn(e0fn(dstType.getEs()), e1fn(dstType.getEs()), e2fn(dstType.getEs()), e3fn(dstType.getEs()), in_pt.y);

        out_pt.x = dstType.getArScaleFactor() * n * al * (1.0 + als / 6.0 * (1.0 - t + c + als / 20.0 * (5.0 - 18.0 * t + t * t + 72.0 * c - 58.0 * dstType.getEsp()))) + dstType.getArFalseEasting();
        out_pt.y = dstType.getArScaleFactor() * (ml - dstType.getDst_m() + n * tq * (als * (0.5 + als / 24.0 * (5.0 - t + 9.0 * c + 4.0 * c * c + als / 30.0 * (61.0 - 58.0 * t + t * t + 600.0 * c - 330.0 * dstType.getEsp()))))) + dstType.getArFalseNorthing();
    }


    private static void tm2geo(@NotNull Coordinate srcType, @NotNull GeoPoint in_pt, GeoPoint out_pt) {
        GeoPoint tmpPt = new GeoPoint(in_pt.getX(), in_pt.getY());
        int max_iter = 6;

        if (srcType.getInd() != 0) {
            double f = Math.exp(in_pt.x / (srcType.getArMajor() * srcType.getArScaleFactor()));
            double g = 0.5 * (f - 1.0 / f);
            double temp = srcType.getArLatCenter() + tmpPt.y / (srcType.getArMajor() * srcType.getArScaleFactor());
            double h = Math.cos(temp);
            double con = Math.sqrt((1.0 - h * h) / (1.0 + g * g));
            out_pt.y = asinz(con);

            if (temp < 0) out_pt.y *= -1;

            if ((g == 0) && (h == 0)) {
                out_pt.x = srcType.getArLonCenter();
            } else {
                out_pt.x = Math.atan(g / h) + srcType.getArLonCenter();
            }
        }

        tmpPt.x -= srcType.getArFalseEasting();
        tmpPt.y -= srcType.getArFalseNorthing();

        double con = (srcType.getSrc_m() + tmpPt.y / srcType.getArScaleFactor()) / srcType.getArMajor();
        double phi = con;

        int i = 0;

        while (true) {
            double delta_Phi = ((con + e1fn(srcType.getEs()) * Math.sin(2.0 * phi) - e2fn(srcType.getEs()) * Math.sin(4.0 * phi) + e3fn(srcType.getEs()) * Math.sin(6.0 * phi)) / e0fn(srcType.getEs())) - phi;
            phi = phi + delta_Phi;

            if (Math.abs(delta_Phi) <= srcType.getEPSLN()) break;

            if (i >= max_iter) {
//                Log.d("무한대 에러");
//                System.out.println("무한대 에러");
                break;
            }

            i++;
        }

        if (Math.abs(phi) < (Math.PI / 2)) {
            double sin_phi = Math.sin(phi);
            double cos_phi = Math.cos(phi);
            double tan_phi = Math.tan(phi);
            double c = srcType.getEsp() * cos_phi * cos_phi;
            double cs = c * c;
            double t = tan_phi * tan_phi;
            double ts = t * t;
            double cont = 1.0 - srcType.getEs() * sin_phi * sin_phi;
            double n = srcType.getArMajor() / Math.sqrt(cont);
            double r = n * (1.0 - srcType.getEs()) / cont;
            double d = tmpPt.x / (n * srcType.getArScaleFactor());
            double ds = d * d;
            out_pt.y = phi - (n * tan_phi * ds / r) * (0.5 - ds / 24.0 * (5.0 + 3.0 * t + 10.0 * c - 4.0 * cs - 9.0 * srcType.getEsp() - ds / 30.0 * (61.0 + 90.0 * t + 298.0 * c + 45.0 * ts - 252.0 * srcType.getEsp() - 3.0 * cs)));
            out_pt.x = srcType.getArLonCenter() + (d * (1.0 - ds / 6.0 * (1.0 + 2.0 * t + c - ds / 20.0 * (5.0 - 2.0 * c + 28.0 * t - 3.0 * cs + 8.0 * srcType.getEsp() + 24.0 * ts))) / cos_phi);
        } else {
            out_pt.y = Math.PI * 0.5 * Math.sin(tmpPt.y);
            out_pt.x = srcType.getArLonCenter();
        }
        transform(srcType, Coordinate.GEO, out_pt);
    }

    private static double getDistancebyGeo(@NotNull GeoPoint pt1, @NotNull GeoPoint pt2) {
        double lat1 = D2R(pt1.y);
        double lon1 = D2R(pt1.x);
        double lat2 = D2R(pt2.y);
        double lon2 = D2R(pt2.x);

        double longitude = lon2 - lon1;
        double latitude = lat2 - lat1;

        double a = Math.pow(Math.sin(latitude / 2.0), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(longitude / 2.0), 2);
        return 6376.5 * 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
    }

    public static double getDistancebyKatec(GeoPoint pt1, GeoPoint pt2) {
        pt1 = convert(Coordinate.KATEC, Coordinate.GEO, pt1);
        pt2 = convert(Coordinate.KATEC, Coordinate.GEO, pt2);

        return getDistancebyGeo(pt1, pt2);
    }

    public static double getDistancebyTm(GeoPoint pt1, GeoPoint pt2) {
        pt1 = convert(Coordinate.TM, Coordinate.GEO, pt1);
        pt2 = convert(Coordinate.TM, Coordinate.GEO, pt2);

        return getDistancebyGeo(pt1, pt2);
    }

    public static double getDistancebyUTMK(GeoPoint pt1, GeoPoint pt2) {
        pt1 = convert(Coordinate.UTMK, Coordinate.GEO, pt1);
        pt2 = convert(Coordinate.UTMK, Coordinate.GEO, pt2);

        return getDistancebyGeo(pt1, pt2);
    }

    public static double getDistancebyGrs80(GeoPoint pt1, GeoPoint pt2) {
        pt1 = convert(Coordinate.GRS80, Coordinate.GEO, pt1);
        pt2 = convert(Coordinate.GRS80, Coordinate.GEO, pt2);

        return getDistancebyGeo(pt1, pt2);
    }

    private static long getTimebySec(double distance) {
        return Math.round(3600 * distance / 4);
    }

    public static long getTimebyMin(double distance) {
        return (long) (Math.ceil(getTimebySec(distance) / 60.0f));
    }

	/*
	Author:       Richard Greenwood rich@greenwoodmap.com
	License:      LGPL as per: http://www.gnu.org/copyleft/lesser.html
	*/

    /**
     * convert between geodetic coordinates (longitude, latitude, height)
     * and gecentric coordinates (X, Y, Z)
     * ported from Proj 4.9.9 geocent.c
     */

    // following constants from geocent.c
    private static final double HALF_PI = 0.5 * Math.PI;
    private static final double COS_67P5 = 0.38268343236508977;  /* cosine of 67.5 degrees */
    private static final double AD_C = 1.0026000;
    /* Toms region 1 constant */

    @Contract(pure = true)
    private static boolean isGRS80Type(Coordinate type) {
        return (type == Coordinate.GRS80 || type == Coordinate.GRS80_EAST || type == Coordinate.GRS80_EASTSEA || type == Coordinate.GRS80_MIDDLE_WITH_JEJUDO || type == Coordinate.GRS80_WEST);
    }

    private static void transform(Coordinate srcType, Coordinate dstType, GeoPoint point) {
        if (srcType == dstType)
            return;

        if ((srcType != Coordinate.GEO && !isGRS80Type(srcType) && srcType != Coordinate.UTMK) || (dstType != Coordinate.GEO && !isGRS80Type(dstType) && dstType != Coordinate.UTMK)) {
            // Convert to geocentric coordinates.
            geodetic_to_geocentric(srcType, point);

            // Convert between datums
            if (srcType != Coordinate.GEO && !isGRS80Type(srcType) && srcType != Coordinate.UTMK) {
                geocentric_to_wgs84(point);
            }

            if (dstType != Coordinate.GEO && !isGRS80Type(dstType) && dstType != Coordinate.UTMK) {
                geocentric_from_wgs84(point);
            }

            // Convert back to geodetic coordinates
            geocentric_to_geodetic(dstType, point);
        }
    }

    private static boolean geodetic_to_geocentric(Coordinate type, @NotNull GeoPoint p) {

        /*
         * The function Convert_Geodetic_To_Geocentric converts geodetic coordinates
         * (latitude, longitude, and height) to geocentric coordinates (X, Y, Z),
         * according to the current ellipsoid parameters.
         *
         *    Latitude  : Geodetic latitude in radians                     (input)
         *    Longitude : Geodetic longitude in radians                    (input)
         *    Height    : Geodetic height, in meters                       (input)
         *    X         : Calculated Geocentric X coordinate, in meters    (output)
         *    Y         : Calculated Geocentric Y coordinate, in meters    (output)
         *    Z         : Calculated Geocentric Z coordinate, in meters    (output)
         *
         */

        double Longitude = p.x;
        double Latitude = p.y;
        double Height = p.z;
        double X;  // output
        double Y;
        double Z;

        double Rn;            /*  Earth radius at location  */
        double Sin_Lat;       /*  Math.sin(Latitude)  */
        double Sin2_Lat;      /*  Square of Math.sin(Latitude)  */
        double Cos_Lat;       /*  Math.cos(Latitude)  */

        /*
         ** Don't blow up if Latitude is just a little out of the value
         ** range as it may just be a rounding issue.  Also removed longitude
         ** test, it should be wrapped by Math.cos() and Math.sin().  NFW for PROJ.4, Sep/2001.
         */
        if (Latitude < -HALF_PI && Latitude > -1.001 * HALF_PI)
            Latitude = -HALF_PI;
        else if (Latitude > HALF_PI && Latitude < 1.001 * HALF_PI)
            Latitude = HALF_PI;
        else if ((Latitude < -HALF_PI) || (Latitude > HALF_PI)) { /* Latitude out of range */
            return true;
        }

        /* no errors */
        if (Longitude > Math.PI)
            Longitude -= (2 * Math.PI);
        Sin_Lat = Math.sin(Latitude);
        Cos_Lat = Math.cos(Latitude);
        Sin2_Lat = Sin_Lat * Sin_Lat;
        Rn = type.getArMajor() / (Math.sqrt(1.0e0 - type.getEs() * Sin2_Lat));
        X = (Rn + Height) * Cos_Lat * Math.cos(Longitude);
        Y = (Rn + Height) * Cos_Lat * Math.sin(Longitude);
        Z = ((Rn * (1 - type.getEs())) + Height) * Sin_Lat;

        p.x = X;
        p.y = Y;
        p.z = Z;
        return false;
    } // cs_geodetic_to_geocentric()


    /**
     * Convert_Geocentric_To_Geodetic
     * The method used here is derived from 'An Improved Algorithm for
     * Geocentric to Geodetic Coordinate Conversion', by Ralph Toms, Feb 1996
     */
    private static void geocentric_to_geodetic(Coordinate type, @NotNull GeoPoint p) {
        double X = p.x;
        double Y = p.y;
        double Z = p.z;
        double Longitude;
        double Latitude = 0.;
        double Height;

        double W;        /* distance from Z axis */
        double W2;       /* square of distance from Z axis */
        double T0;       /* initial estimate of vertical component */
        double T1;       /* corrected estimate of vertical component */
        double S0;       /* initial estimate of horizontal component */
        double S1;       /* corrected estimate of horizontal component */
        double Sin_B0;   /* Math.sin(B0), B0 is estimate of Bowring aux doubleiable */
        double Sin3_B0;  /* cube of Math.sin(B0) */
        double Cos_B0;   /* Math.cos(B0) */
        double Sin_p1;   /* Math.sin(phi1), phi1 is estimated latitude */
        double Cos_p1;   /* Math.cos(phi1) */
        double Rn;       /* Earth radius at location */
        double Sum;      /* numerator of Math.cos(phi1) */
        boolean At_Pole;  /* indicates location is in polar region */

        At_Pole = false;
        if (X != 0.0) {
            Longitude = Math.atan2(Y, X);
        } else {
            if (Y > 0) {
                Longitude = HALF_PI;
            } else if (Y < 0) {
                Longitude = -HALF_PI;
            } else {
                At_Pole = true;
                Longitude = 0.0;
                if (Z > 0.0) {  /* north pole */
                    Latitude = HALF_PI;
                } else if (Z < 0.0) {  /* south pole */
                    Latitude = -HALF_PI;
                } else {  /* center of earth */
                    Latitude = HALF_PI;
                    Height = -type.getArMinor();
                    return;
                }
            }
        }
        W2 = X * X + Y * Y;
        W = Math.sqrt(W2);
        T0 = Z * AD_C;
        S0 = Math.sqrt(T0 * T0 + W2);
        Sin_B0 = T0 / S0;
        Cos_B0 = W / S0;
        Sin3_B0 = Sin_B0 * Sin_B0 * Sin_B0;
        T1 = Z + type.getArMinor() * type.getEsp() * Sin3_B0;
        Sum = W - type.getArMajor() * type.getEs() * Cos_B0 * Cos_B0 * Cos_B0;
        S1 = Math.sqrt(T1 * T1 + Sum * Sum);
        Sin_p1 = T1 / S1;
        Cos_p1 = Sum / S1;
        Rn = type.getArMajor() / Math.sqrt(1.0 - type.getEs() * Sin_p1 * Sin_p1);
        if (Cos_p1 >= COS_67P5) {
            Height = W / Cos_p1 - Rn;
        } else if (Cos_p1 <= -COS_67P5) {
            Height = W / -Cos_p1 - Rn;
        } else {
            Height = Z / Sin_p1 + Rn * (type.getEs() - 1.0);
        }
        if (!At_Pole) {
            Latitude = Math.atan(Sin_p1 / Cos_p1);
        }

        p.x = Longitude;
        p.y = Latitude;
        p.z = Height;
        return;
    } // geocentric_to_geodetic()


    /****************************************************************/
    // geocentic_to_wgs84(defn, p )
    //  defn = coordinate system definition,
    //  p = point to transform in geocentric coordinates (x,y,z)
    private static void geocentric_to_wgs84(@NotNull GeoPoint p) {

        //if( defn.datum_type == PJD_3PARAM )
        {
            // if( x[io] == HUGE_VAL )
            //    continue;
            p.x += Coordinate.GEO.getDatumX();
            p.y += Coordinate.GEO.getDatumY();
            p.z += Coordinate.GEO.getDatumZ();
        }
    } // geocentric_to_wgs84

    /****************************************************************/
    // geocentic_from_wgs84()
    //  coordinate system definition,
    //  point to transform in geocentric coordinates (x,y,z)
    private static void geocentric_from_wgs84(@NotNull GeoPoint p) {

        //if( defn.datum_type == PJD_3PARAM )
        {
            //if( x[io] == HUGE_VAL )
            //    continue;
            p.x -= Coordinate.GEO.getDatumX();
            p.y -= Coordinate.GEO.getDatumY();
            p.z -= Coordinate.GEO.getDatumZ();

        }
    } //geocentric_from_wgs84()

    @Getter
    public enum Coordinate {
        GEO(6378137.0, 6356752.3142, 1, 0.0, 0.0, 0.0, 0.0),
        KATEC(6377397.155, 6356078.9633422494, 0.9999, 2.23402144255274, 0.663225115757845, 600000.0, 400000.0),
        TM(6377397.155, 6356078.9633422494, 1.0, 2.21661859489671, 0.663225115757845, 500000.0, 200000.0),
        UTMK(6378137.0, 6356752.3141403558, 0.9996, 2.22529479629277, 0.663225115757845, 2000000.0, 1000000.0),
        GRS80(6378137.0, 6356752.3142, 1.0, 2.21661859489671, 0.663225115757845, 500000.0, 200000.0),
        GRS80_EAST(6378137.0, 6356752.3142, 1.0, Math.toRadians(129), 0.663225115757845, 600000.0, 200000.0),
        GRS80_WEST(6378137.0, 6356752.3142, 1.0, Math.toRadians(125), 0.663225115757845, 600000.0, 200000.0),
        GRS80_MIDDLE_WITH_JEJUDO(6378137.0, 6356752.3142, 1.0, Math.toRadians(127), 0.663225115757845, 600000.0, 200000.0),
        GRS80_EASTSEA(6378137.0, 6356752.3142, 1.0, Math.toRadians(131), 0.663225115757845, 600000.0, 200000.0);

        private final double EPSLN = 0.0000000001;
        private final double arMajor;
        private final double arMinor;

        private final double arScaleFactor;
        private final double arLonCenter;
        private final double arLatCenter;
        private final double arFalseNorthing;
        private final double arFalseEasting;

        private final double es;
        private final double esp;
        private final double Ind;
        private final double src_m;
        private final double dst_m;

        private final double datumX = -147;
        private final double datumY = 506;
        private final double datumZ = 687;

        Coordinate(double arMajor, double arMinor, double arScaleFactor, double arLonCenter, double arLatCenter, double arFalseNorthing, double arFalseEasting) {
            this.arMajor = arMajor;
            this.arMinor = arMinor;
            this.arScaleFactor = arScaleFactor;
            this.arLonCenter = arLonCenter;
            this.arLatCenter = arLatCenter;
            this.arFalseNorthing = arFalseNorthing;
            this.arFalseEasting = arFalseEasting;

            double temp = arMinor / arMajor;
            this.es = 1.0 - temp * temp;
            this.esp = this.es / (1.0 - this.es);

            if (this.es < 0.00001) {
                this.Ind = 1.0;
            } else {
                this.Ind = 0.0;
            }

            this.src_m = arMajor * GeoTrans.mlfn(GeoTrans.e0fn(this.es), GeoTrans.e1fn(this.es), GeoTrans.e2fn(this.es), GeoTrans.e3fn(this.es), arLatCenter);
            this.dst_m = arMajor * GeoTrans.mlfn(GeoTrans.e0fn(this.es), GeoTrans.e1fn(this.es), GeoTrans.e2fn(this.es), GeoTrans.e3fn(this.es), arLatCenter);
        }

        @NotNull
        @Override
        @Contract(pure = true)
        public String toString() {
            return "Coordinate{" + "EPSLN=" + EPSLN + ", arMajor=" + arMajor + ", arMinor=" + arMinor + ", arScaleFactor=" + arScaleFactor + ", arLonCenter=" + arLonCenter + ", arLatCenter=" + arLatCenter + ", arFalseNorthing=" + arFalseNorthing + ", arFalseEasting=" + arFalseEasting + ", es=" + es + ", esp=" + esp + ", Ind=" + Ind + ", src_m=" + src_m + ", dst_m=" + dst_m + ", datumX=" + datumX + ", datumY=" + datumY + ", datumZ=" + datumZ + '}';
        }
    }
}
