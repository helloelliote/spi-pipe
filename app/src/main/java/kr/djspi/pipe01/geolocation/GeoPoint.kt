package kr.djspi.pipe01.geolocation

class GeoPoint {
    var x: Double = 0.toDouble()
    var y: Double = 0.toDouble()
    var z: Double = 0.toDouble()

    internal constructor() : super()

    constructor(x: Double, y: Double) : super() {
        this.x = x
        this.y = y
        this.z = 0.0
    }
}
