package kr.djspi.pipe01

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import com.google.gson.JsonObject
import com.helloelliote.util.geolocation.GeoPoint
import com.helloelliote.util.geolocation.GeoTrans.Coordinate.GEO
import com.helloelliote.util.geolocation.GeoTrans.convert
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.CameraUpdate.REASON_GESTURE
import com.naver.maps.map.LocationTrackingMode.None
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MapConstants
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_spi_location.*
import kr.djspi.pipe01.AppPreference.get
import kr.djspi.pipe01.BuildConfig.CLIENT_ID
import kr.djspi.pipe01.Const.RESULT_PASS
import kr.djspi.pipe01.Const.TAG_SURVEY
import kr.djspi.pipe01.dto.PipeType
import kr.djspi.pipe01.fragment.OnSelectListener
import kr.djspi.pipe01.fragment.SurveyDialog
import kr.djspi.pipe01.fragment.SurveyDialog.originPoint
import kr.djspi.pipe01.network.Retrofit2x
import kr.djspi.pipe01.util.*
import java.io.Serializable
import java.util.*

class SpiLocationActivity :
    LocationUpdate(), OnMapReadyCallback, View.OnClickListener, OnSelectListener, Serializable {

    private lateinit var surveyDialog: SurveyDialog
    private lateinit var naverMap: NaverMap
    private var isSelected = true

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        NaverMapSdk.getInstance(this).client = NaverMapSdk.NaverCloudPlatformClient(CLIENT_ID)
        setContentView(R.layout.activity_spi_location)
        setNaverMap()
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        record_gps.setOnClickListener(this)
        btn_confirm.setOnClickListener(this)

        toolbar.title = getString(R.string.record_location_title)
        nmap_find.visibility = View.GONE
    }

    private fun setNaverMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance(
                NaverMapOptions()
                    .locale(Locale.KOREA)
                    .camera(CameraPosition(LatLng(currentLocation!!), ZOOM_DEFAULT, 0.0, 0.0))
                    .enabledLayerGroups(NaverMap.LAYER_GROUP_BUILDING)
                    .minZoom(ZOOM_MIN)
                    .maxZoom(ZOOM_MAX)
                    .extent(MapConstants.EXTENT_KOREA)
                    .compassEnabled(true)
                    .locationButtonEnabled(true)
                    .zoomGesturesEnabled(true)
            ).also {
                supportFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.apply {
            this.locationSource = locationSource
            locationTrackingMode = None
            addOnCameraChangeListener { reason, _ ->
                if (reason == REASON_GESTURE) record_gps.alpha = 0.25f
            }
        }

        setMapModeSwitch(naverMap)

        val preferences = AppPreference.defaultPrefs(this)
        if (preferences["switch_location", false]!!) {
            runOnUiThread {
                record_gps.visibility = View.INVISIBLE
                surveyDialog = SurveyDialog().apply {
                    isCancelable = false
                }
                surveyDialog.show(supportFragmentManager, TAG_SURVEY)
            }
        }

        Thread(Runnable {
            onRequestPipe(naverMap)
        }).start()
    }

    /**
     * Toolbar 에서 지도 모드 전환 스위치 구현
     *
     * @param naverMap API 를 호출하는 인터페이스 역할을 하는 NaverMapActivity 객체
     */
    private fun setMapModeSwitch(naverMap: NaverMap) {
        findViewById<ToggleSwitch>(R.id.nmap_mapmode_switch).apply {
            visibility = View.VISIBLE
            checkedPosition = 0
            onChangeListener = object : ToggleSwitch.OnChangeListener {
                override fun onToggleSwitchChanged(position: Int) {
                    when (position) {
                        0 -> naverMap.mapType = NaverMap.MapType.Basic
                        1 -> naverMap.mapType = NaverMap.MapType.Hybrid
                        else -> naverMap.mapType = NaverMap.MapType.Basic
                    }
                }
            }
        }
    }

    private fun onRequestPipe(naverMap: NaverMap) {
        val jsonQuery = JsonObject()
        val bounds = naverMap.contentBounds
        jsonQuery.addProperty("sx", bounds.westLongitude.toString())
        jsonQuery.addProperty("sy", bounds.southLatitude.toString())
        jsonQuery.addProperty("nx", bounds.eastLongitude.toString())
        jsonQuery.addProperty("ny", bounds.northLatitude.toString())

        Retrofit2x.getSpi("pipe-get", jsonQuery).enqueue(object : RetrofitCallback() {
            override fun onResponse(response: JsonObject) {
                if (response["total_count"].asInt != 0) {
                    response["data"].asJsonArray.forEach { element ->
                        val jsonObject = element.asJsonObject
                        val lat = jsonObject["spi_latitude"].asDouble
                        val lng = jsonObject["spi_longitude"].asDouble
                        val resId = PipeType.parsePipeType(jsonObject["pipe"].asString).drawRes
                        Marker(LatLng(lat, lng), OverlayImage.fromResource(resId)).apply {
                            minZoom = ZOOM_GET
                            maxZoom = ZOOM_MAX
                        }.run {
                            this.map = naverMap
                        }
                    }
                }
            }

            override fun onFailure(throwable: Throwable) {
//                messageDialog(8, throwable.message)
            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.record_gps -> {
                v.alpha = 1.0f
            }
            R.id.btn_confirm -> {
                try {
                    val latLng = naverMap.cameraPosition.target
                    val spiLocation = doubleArrayOf(latLng.latitude, latLng.longitude)
                    setResult(RESULT_OK, Intent().putExtra("locations", spiLocation))
                    finish()
                } catch (e: Exception) {
                    messageDialog(0, getString(R.string.toast_error_location))
                    return
                }

            }
        }
    }

    override fun onSelect(tag: String?, index: Int, vararg text: String?) {
        when (tag) {
            TAG_SURVEY -> {
                if (index == RESULT_PASS) {
                    val surveyLatLng = convertTmToLatLng(text[0]!!.toDouble(), text[1]!!.toDouble())
                    val cameraUpdate = CameraUpdate
                        .scrollAndZoomTo(surveyLatLng, ZOOM_DEFAULT)
                        .animate(CameraAnimation.Fly)
                    naverMap.moveCamera(cameraUpdate)
                } else {
                    isSelected = false
                    onBackPressed()
                }
            }
        }
    }

    /**
     * Tm 좌표계를 LatLng 경위도 좌표계로 변환한다. 반드시 변환 과정에서 X, Y 좌표를 반전시켜 리턴한다.
     */
    private fun convertTmToLatLng(Tm1: Double, Tm2: Double): LatLng {
        val surveyPoint = convert(originPoint, GEO, GeoPoint(Tm2, Tm1))
        return LatLng(surveyPoint.y, surveyPoint.x)
    }

    override fun onResume() {
        super.onResume()
        onResumeNfc()
    }

    override fun onPause() {
        super.onPause()
        onPauseNfc()
    }

    override fun onStop() {
        super.onStop()
        originPoint = null
    }

    override fun onBackPressed() {
        if (surveyDialog.isAdded && isSelected) return
        super.onBackPressed()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        onNewIntentIgnore()
    }

    companion object {
        private const val ZOOM_DEFAULT = 18.0 // 기본 줌레벨
        private const val ZOOM_MIN = 6.0 // 최소 줌레벨
        private const val ZOOM_GET = 12.0
        private const val ZOOM_MAX = NaverMap.MAXIMUM_ZOOM.toDouble() // 최대 줌레벨(21)
    }
}
