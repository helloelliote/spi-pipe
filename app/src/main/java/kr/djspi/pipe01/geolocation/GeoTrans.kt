package kr.djspi.pipe01.geolocation

import java.lang.Math.pow
import kotlin.math.*
import kr.djspi.pipe01.geolocation.GeoTrans.Coordinate.*

/**
 * convert between geodetic coordinates (longitude, latitude, height)
 * and gecentric coordinates (X, Y, Z)
 * ported from Proj 4.9.9 geocent.c
 *
 * @author Richard Greenwood rich@greenwoodmap.com
 * LGPL as per: http://www.gnu.org/copyleft/lesser.html
 */
@Suppress(
    "FunctionName", "SpellCheckingInspection", "LocalVariableName", "unused",
    "PropertyName"
)
object GeoTrans {

    // following constants from geocent.c
    private const val HALF_PI = 0.5 * Math.PI
    private const val COS_67P5 = 0.38268343236508977 /* cosine of 67.5 degrees */
    private const val AD_C = 1.0026000

    private fun D2R(degree: Double): Double = degree * Math.PI / 180.0

    private fun R2D(radian: Double): Double = radian * 180.0 / Math.PI

    private fun e0fn(x: Double): Double = 1.0 - 0.25 * x * (1.0 + x / 16.0 * (3.0 + 1.25 * x))

    private fun e1fn(x: Double): Double = 0.375 * x * (1.0 + 0.25 * x * (1.0 + 0.46875 * x))

    private fun e2fn(x: Double): Double = 0.05859375 * x * x * (1.0 + 0.75 * x)

    private fun e3fn(x: Double): Double = x * x * x * (35.0 / 3072.0)

    private fun mlfn(e0: Double, e1: Double, e2: Double, e3: Double, phi: Double): Double =
        e0 * phi - e1 * sin(2.0 * phi) + e2 * sin(4.0 * phi) - e3 * sin(6.0 * phi)

    private fun asinz(d0: Double): Double {
        var value = d0
        if (abs(value) > 1.0) value = (if (value > 0) 1 else -1).toDouble()
        return asin(value)
    }

    fun convert(srcType: Coordinate, dstType: Coordinate, in_pt: GeoPoint): GeoPoint {
        val tmpPt = GeoPoint()
        val outPt = GeoPoint()

        if (srcType == GEO) {
            tmpPt.x = D2R(in_pt.x)
            tmpPt.y = D2R(in_pt.y)
        } else {
            tm2geo(srcType, in_pt, tmpPt)
        }

        if (dstType == GEO) {
            outPt.x = R2D(tmpPt.x)
            outPt.y = R2D(tmpPt.y)
        } else {
            geo2tm(dstType, tmpPt, outPt)
            //            out_pt.x = Math.round(out_pt.x);
            //            out_pt.y = Math.round(out_pt.y);
        }
        return outPt
    }

    private fun geo2tm(dstType: Coordinate, in_pt: GeoPoint, out_pt: GeoPoint) {
        val x: Double
        val y: Double

        transform(GEO, dstType, in_pt)
        val delta_lon = in_pt.x - dstType.arLonCenter
        val sin_phi = sin(in_pt.y)
        val cos_phi = cos(in_pt.y)

        if (dstType.ind != 0.0) {
            val b = cos_phi * sin(delta_lon)

            if (abs(abs(b) - 1.0) < dstType.epsln) {
                //                Log.d("무한대 에러");
                //                System.out.println("무한대 에러");
            }
        } else {
            val b = 0.0
            x = 0.5 * dstType.arMajor * dstType.arScaleFactor * ln((1.0 + b) / (1.0 - b))
            var con = acos(cos_phi * cos(delta_lon) / sqrt(1.0 - b * b))

            if (in_pt.y < 0) {
                con *= -1
                y = dstType.arMajor * dstType.arScaleFactor * (con - dstType.arLatCenter)
            }
        }

        val al = cos_phi * delta_lon
        val als = al * al
        val c = dstType.esp * cos_phi * cos_phi
        val tq = tan(in_pt.y)
        val t = tq * tq
        val con = 1.0 - dstType.es * sin_phi * sin_phi
        val n = dstType.arMajor / sqrt(con)
        val ml = dstType.arMajor * mlfn(
            e0fn(dstType.es),
            e1fn(dstType.es),
            e2fn(dstType.es),
            e3fn(dstType.es),
            in_pt.y
        )

        out_pt.x =
            dstType.arScaleFactor * n * al * (1.0 + als / 6.0 * (1.0 - t + c + als / 20.0 * (5.0 - 18.0 * t + t * t + 72.0 * c - 58.0 * dstType.esp))) + dstType.arFalseEasting
        out_pt.y =
            dstType.arScaleFactor * (ml - dstType.dst_m + n * tq * (als * (0.5 + als / 24.0 * (5.0 - t + 9.0 * c + 4.0 * c * c + als / 30.0 * (61.0 - 58.0 * t + t * t + 600.0 * c - 330.0 * dstType.esp))))) + dstType.arFalseNorthing
    }

    private fun tm2geo(srcType: Coordinate, in_pt: GeoPoint, out_pt: GeoPoint) {
        val tmpPt = GeoPoint(in_pt.x, in_pt.y)
        val max_iter = 6

        if (srcType.ind != 0.0) {
            val f = exp(in_pt.x / (srcType.arMajor * srcType.arScaleFactor))
            val g = 0.5 * (f - 1.0 / f)
            val temp = srcType.arLatCenter + tmpPt.y / (srcType.arMajor * srcType.arScaleFactor)
            val h = cos(temp)
            val con = sqrt((1.0 - h * h) / (1.0 + g * g))
            out_pt.y = asinz(con)

            if (temp < 0) out_pt.y = out_pt.y * -1

            if (g == 0.0 && h == 0.0) {
                out_pt.x = srcType.arLonCenter
            } else {
                out_pt.x = atan(g / h) + srcType.arLonCenter
            }
        }

        tmpPt.x = tmpPt.x - srcType.arFalseEasting
        tmpPt.y = tmpPt.y - srcType.arFalseNorthing

        val con = (srcType.src_m + tmpPt.y / srcType.arScaleFactor) / srcType.arMajor
        var phi = con

        var i = 0

        while (true) {
            val delta_Phi =
                (con + e1fn(srcType.es) * sin(2.0 * phi) - e2fn(srcType.es) * sin(4.0 * phi) + e3fn(
                    srcType.es
                ) * sin(6.0 * phi)) / e0fn(srcType.es) - phi
            phi += delta_Phi

            if (abs(delta_Phi) <= srcType.epsln) break

            if (i >= max_iter) {
                //                Log.d("무한대 에러");
                //                System.out.println("무한대 에러");
                break
            }

            i++
        }

        if (abs(phi) < Math.PI / 2) {
            val sin_phi = sin(phi)
            val cos_phi = cos(phi)
            val tan_phi = tan(phi)
            val c = srcType.esp * cos_phi * cos_phi
            val cs = c * c
            val t = tan_phi * tan_phi
            val ts = t * t
            val cont = 1.0 - srcType.es * sin_phi * sin_phi
            val n = srcType.arMajor / sqrt(cont)
            val r = n * (1.0 - srcType.es) / cont
            val d = tmpPt.x / (n * srcType.arScaleFactor)
            val ds = d * d
            out_pt.y =
                phi - n * tan_phi * ds / r * (0.5 - ds / 24.0 * (5.0 + 3.0 * t + 10.0 * c - 4.0 * cs - 9.0 * srcType.esp - ds / 30.0 * (61.0 + 90.0 * t + 298.0 * c + 45.0 * ts - 252.0 * srcType.esp - 3.0 * cs)))
            out_pt.x =
                srcType.arLonCenter + d * (1.0 - ds / 6.0 * (1.0 + 2.0 * t + c - ds / 20.0 * (5.0 - 2.0 * c + 28.0 * t - 3.0 * cs + 8.0 * srcType.esp + 24.0 * ts))) / cos_phi
        } else {
            out_pt.y = Math.PI * 0.5 * sin(tmpPt.y)
            out_pt.x = srcType.arLonCenter
        }
        transform(srcType, GEO, out_pt)
    }

    private fun getDistancebyGeo(pt1: GeoPoint, pt2: GeoPoint): Double {
        val lat1 = D2R(pt1.y)
        val lon1 = D2R(pt1.x)
        val lat2 = D2R(pt2.y)
        val lon2 = D2R(pt2.x)

        val longitude = lon2 - lon1
        val latitude = lat2 - lat1

        val a =
            pow(sin(latitude / 2.0), 2.0) + cos(lat1) * cos(lat2) * pow(
                sin(longitude / 2.0),
                2.0
            )
        return 6376.5 * 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
    }

    fun getDistancebyKatec(p1: GeoPoint, p2: GeoPoint): Double {
        var pt1 = p1
        var pt2 = p2
        pt1 = convert(KATEC, GEO, pt1)
        pt2 = convert(KATEC, GEO, pt2)
        return getDistancebyGeo(pt1, pt2)
    }

    fun getDistancebyTm(p1: GeoPoint, p2: GeoPoint): Double {
        var pt1 = p1
        var pt2 = p2
        pt1 = convert(TM, GEO, pt1)
        pt2 = convert(TM, GEO, pt2)
        return getDistancebyGeo(pt1, pt2)
    }

    fun getDistancebyUTMK(p1: GeoPoint, p2: GeoPoint): Double {
        var pt1 = p1
        var pt2 = p2
        pt1 = convert(UTMK, GEO, pt1)
        pt2 = convert(UTMK, GEO, pt2)
        return getDistancebyGeo(pt1, pt2)
    }

    fun getDistancebyGrs80(p1: GeoPoint, p2: GeoPoint): Double {
        var pt1 = p1
        var pt2 = p2
        pt1 = convert(GRS80, GEO, pt1)
        pt2 = convert(GRS80, GEO, pt2)
        return getDistancebyGeo(pt1, pt2)
    }

    private fun getTimebySec(distance: Double): Long {
        return (3600 * distance / 4).roundToLong()
    }

    fun getTimebyMin(distance: Double): Long {
        return ceil((getTimebySec(distance) / 60.0f).toDouble()).toLong()
    }
    /* Toms region 1 constant */

    private fun isGRS80Type(type: Coordinate): Boolean {
        return type == GRS80 || type == GRS80_EAST || type == GRS80_EASTSEA || type == GRS80_MIDDLE_WITH_JEJUDO || type == GRS80_WEST
    }

    private fun transform(srcType: Coordinate, dstType: Coordinate, point: GeoPoint) {
        if (srcType == dstType)
            return

        if (srcType != GEO && !isGRS80Type(srcType) && srcType != UTMK || dstType != GEO && !isGRS80Type(
                dstType
            ) && dstType != UTMK
        ) {
            // Convert to geocentric coordinates.
            geodetic_to_geocentric(srcType, point)

            // Convert between datums
            if (srcType != GEO && !isGRS80Type(srcType) && srcType != UTMK) {
                geocentric_to_wgs84(point)
            }

            if (dstType != GEO && !isGRS80Type(dstType) && dstType != UTMK) {
                geocentric_from_wgs84(point)
            }

            // Convert back to geodetic coordinates
            geocentric_to_geodetic(dstType, point)
        }
    }

    private fun geodetic_to_geocentric(type: Coordinate, p: GeoPoint): Boolean {

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

        var Longitude = p.x
        var Latitude = p.y
        val Height = p.z
        val X: Double // output
        val Y: Double
        val Z: Double

        val Rn: Double /*  Earth radius at location  */
        val Sin_Lat: Double /*  sin(Latitude)  */
        val Sin2_Lat: Double /*  Square of sin(Latitude)  */
        val Cos_Lat: Double /*  cos(Latitude)  */

        /*
         ** Don't blow up if Latitude is just a little out of the value
         ** range as it may just be a rounding issue.  Also removed longitude
         ** test, it should be wrapped by cos() and sin().  NFW for PROJ.4, Sep/2001.
         */
        if (Latitude < -HALF_PI && Latitude > -1.001 * HALF_PI)
            Latitude = -HALF_PI
        else if (Latitude > HALF_PI && Latitude < 1.001 * HALF_PI)
            Latitude = HALF_PI
        else if (Latitude < -HALF_PI || Latitude > HALF_PI) { /* Latitude out of range */
            return true
        }

        /* no errors */
        if (Longitude > Math.PI)
            Longitude -= 2 * Math.PI
        Sin_Lat = sin(Latitude)
        Cos_Lat = cos(Latitude)
        Sin2_Lat = Sin_Lat * Sin_Lat
        Rn = type.arMajor / sqrt(1.0e0 - type.es * Sin2_Lat)
        X = (Rn + Height) * Cos_Lat * cos(Longitude)
        Y = (Rn + Height) * Cos_Lat * sin(Longitude)
        Z = (Rn * (1 - type.es) + Height) * Sin_Lat

        p.x = X
        p.y = Y
        p.z = Z
        return false
    } // cs_geodetic_to_geocentric()

    /**
     * Convert_Geocentric_To_Geodetic
     * The method used here is derived from 'An Improved Algorithm for
     * Geocentric to Geodetic Coordinate Conversion', by Ralph Toms, Feb 1996
     */
    private fun geocentric_to_geodetic(type: Coordinate, p: GeoPoint) {
        val X = p.x
        val Y = p.y
        val Z = p.z
        val Longitude: Double
        var Latitude = 0.0
        val Height: Double

        val W: Double /* distance from Z axis */
        val W2: Double /* square of distance from Z axis */
        val T0: Double /* initial estimate of vertical component */
        val T1: Double /* corrected estimate of vertical component */
        val S0: Double /* initial estimate of horizontal component */
        val S1: Double /* corrected estimate of horizontal component */
        val Sin_B0: Double /* sin(B0), B0 is estimate of Bowring aux doubleiable */
        val Sin3_B0: Double /* cube of sin(B0) */
        val Cos_B0: Double /* cos(B0) */
        val Sin_p1: Double /* sin(phi1), phi1 is estimated latitude */
        val Cos_p1: Double /* cos(phi1) */
        val Rn: Double /* Earth radius at location */
        val Sum: Double /* numerator of cos(phi1) */
        var At_Pole: Boolean /* indicates location is in polar region */

        At_Pole = false
        if (X != 0.0) {
            Longitude = atan2(Y, X)
        } else {
            when {
                Y > 0 -> Longitude = HALF_PI
                Y < 0 -> Longitude = -HALF_PI
                else -> {
                    At_Pole = true
                    Longitude = 0.0
                    when {
                        Z > 0.0 -> /* north pole */
                            Latitude = HALF_PI
                        Z < 0.0 -> /* south pole */
                            Latitude = -HALF_PI
                        else -> { /* center of earth */
                            Latitude = HALF_PI
                            Height = -type.arMinor
                            return
                        }
                    }
                }
            }
        }
        W2 = X * X + Y * Y
        W = sqrt(W2)
        T0 = Z * AD_C
        S0 = sqrt(T0 * T0 + W2)
        Sin_B0 = T0 / S0
        Cos_B0 = W / S0
        Sin3_B0 = Sin_B0 * Sin_B0 * Sin_B0
        T1 = Z + type.arMinor * type.esp * Sin3_B0
        Sum = W - type.arMajor * type.es * Cos_B0 * Cos_B0 * Cos_B0
        S1 = sqrt(T1 * T1 + Sum * Sum)
        Sin_p1 = T1 / S1
        Cos_p1 = Sum / S1
        Rn = type.arMajor / sqrt(1.0 - type.es * Sin_p1 * Sin_p1)
        Height = when {
            Cos_p1 >= COS_67P5 -> W / Cos_p1 - Rn
            Cos_p1 <= -COS_67P5 -> W / -Cos_p1 - Rn
            else -> Z / Sin_p1 + Rn * (type.es - 1.0)
        }
        if (!At_Pole) {
            Latitude = atan(Sin_p1 / Cos_p1)
        }

        p.x = Longitude
        p.y = Latitude
        p.z = Height
        return
    } // geocentric_to_geodetic()

    /**
     * geocentic_to_wgs84(defn, p)
     * defn = coordinate system definition,
     * p = point to transform in geocentric coordinates (x,y,z)
     */
    private fun geocentric_to_wgs84(p: GeoPoint) {
        // if( defn.datum_type == PJD_3PARAM )
        // if( x[io] == HUGE_VAL )
        //    continue;
        p.x = p.x + GEO.datumX
        p.y = p.y + GEO.datumY
        p.z = p.z + GEO.datumZ
    } // geocentric_to_wgs84

    /**
     *  geocentic_from_wgs84()
     *  defn = coordinate system definition
     *  p = point to transform in geocentric coordinates (x,y,z)
     */
    private fun geocentric_from_wgs84(p: GeoPoint) {
        // if( defn.datum_type == PJD_3PARAM )
        // if( x[io] == HUGE_VAL )
        //    continue;
        p.x = p.x - GEO.datumX
        p.y = p.y - GEO.datumY
        p.z = p.z - GEO.datumZ
    } // geocentric_from_wgs84()

    enum class Coordinate(
        val arMajor: Double,
        val arMinor: Double,
        val arScaleFactor: Double,
        val arLonCenter: Double,
        val arLatCenter: Double,
        val arFalseNorthing: Double,
        val arFalseEasting: Double
    ) {
        GEO(
            6378137.0,
            6356752.3142,
            1.0,
            0.0,
            0.0,
            0.0,
            0.0
        ),
        KATEC(
            6377397.155,
            6356078.9633422494,
            0.9999,
            2.23402144255274,
            0.663225115757845,
            600000.0,
            400000.0
        ),
        TM(
            6377397.155,
            6356078.9633422494,
            1.0,
            2.21661859489671,
            0.663225115757845,
            500000.0,
            200000.0
        ),
        UTMK(
            6378137.0,
            6356752.3141403558,
            0.9996,
            2.22529479629277,
            0.663225115757845,
            2000000.0,
            1000000.0
        ),
        GRS80(
            6378137.0,
            6356752.3142,
            1.0,
            2.21661859489671,
            0.663225115757845,
            500000.0,
            200000.0
        ),
        GRS80_EAST(
            6378137.0,
            6356752.3142,
            1.0,
            Math.toRadians(129.0),
            0.663225115757845,
            600000.0,
            200000.0
        ),
        GRS80_WEST(
            6378137.0,
            6356752.3142,
            1.0,
            Math.toRadians(125.0),
            0.663225115757845,
            600000.0,
            200000.0
        ),
        GRS80_MIDDLE_WITH_JEJUDO(
            6378137.0,
            6356752.3142,
            1.0,
            Math.toRadians(127.0),
            0.663225115757845,
            600000.0,
            200000.0
        ),
        GRS80_EASTSEA(
            6378137.0,
            6356752.3142,
            1.0,
            Math.toRadians(131.0),
            0.663225115757845,
            600000.0,
            200000.0
        );

        val epsln = 0.0000000001

        val es: Double
        val esp: Double
        val ind: Double
        val src_m: Double
        val dst_m: Double

        val datumX = -147.0
        val datumY = 506.0
        val datumZ = 687.0

        init {
            val temp = arMinor / arMajor
            this.es = 1.0 - temp * temp
            this.esp = this.es / (1.0 - this.es)

            if (this.es < 0.00001) {
                this.ind = 1.0
            } else {
                this.ind = 0.0
            }

            this.src_m = arMajor * mlfn(
                e0fn(this.es),
                e1fn(this.es),
                e2fn(this.es),
                e3fn(this.es),
                arLatCenter
            )
            this.dst_m = arMajor * mlfn(
                e0fn(this.es),
                e1fn(this.es),
                e2fn(this.es),
                e3fn(this.es),
                arLatCenter
            )
        }

        override fun toString(): String {
            return "Coordinate{EPSLN=$epsln, arMajor=$arMajor, arMinor=$arMinor, arScaleFactor=$arScaleFactor, arLonCenter=$arLonCenter, arLatCenter=$arLatCenter, arFalseNorthing=$arFalseNorthing, arFalseEasting=$arFalseEasting, es=$es, esp=$esp, Ind=$ind, src_m=$src_m, dst_m=$dst_m, datumX=$datumX, datumY=$datumY, datumZ=$datumZ}"
        }
    }
}
