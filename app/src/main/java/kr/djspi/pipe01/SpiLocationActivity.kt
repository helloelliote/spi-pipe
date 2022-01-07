package kr.djspi.pipe01

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.annotation.UiThread
import androidx.core.view.get
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.gson.JsonObject
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Marker.DEFAULT_ANCHOR
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MapConstants
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_spi_location.*
import kr.djspi.pipe01.AppPreference.get
import kr.djspi.pipe01.AppPreference.set
import kr.djspi.pipe01.BuildConfig.CLIENT_ID
import kr.djspi.pipe01.Const.RESULT_PASS
import kr.djspi.pipe01.Const.TAG_SURVEY_PIPE
import kr.djspi.pipe01.Const.TAG_SURVEY_SPI
import kr.djspi.pipe01.dto.PipeType.PipeTypeEnum.Companion.parsePipeType
import kr.djspi.pipe01.fragment.OnSelectListener
import kr.djspi.pipe01.fragment.SurveyDialog
import kr.djspi.pipe01.fragment.SurveyDialog.Companion.originPoint
import kr.djspi.pipe01.fragment.SurveyDialog2
import kr.djspi.pipe01.geolocation.GeoPoint
import kr.djspi.pipe01.geolocation.GeoTrans
import kr.djspi.pipe01.geolocation.GeoTrans.convert
import kr.djspi.pipe01.network.Retrofit2x
import kr.djspi.pipe01.util.*
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

class SpiLocationActivity :
    LocationUpdate(), OnMapReadyCallback, OnClickListener, OnSelectListener, Serializable {

    private var surveyDialog: SurveyDialog = SurveyDialog()
    private var surveyDialog2: SurveyDialog2 = SurveyDialog2()
    private var naverMap: NaverMap? = null
    private var mapFragment: MapFragment? = null
    private val spiLocationMap = HashMap<String, Any?>(5)
    private val pipeLocationMap = HashMap<String, Any?>(5)
    private lateinit var spiLatLng: LatLng
    private lateinit var pipeLatLng: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        onPipeSurveyDialog()
    }

    /**
     * 네이버 지도의 기초 UI 설정
     */
    @UiThread
    private fun setNaverMap() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?
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
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(naverMap0: NaverMap) {
        this.naverMap = naverMap0
        val fusedLocationSource = FusedLocationSource(this, 100)
        naverMap!!.apply {
            locationSource = fusedLocationSource
            locationTrackingMode = LocationTrackingMode.Follow
            addOnOptionChangeListener {
                val mode: LocationTrackingMode = this.locationTrackingMode
                fusedLocationSource.isCompassEnabled =
                    mode == LocationTrackingMode.Follow || mode == LocationTrackingMode.Face
            }
        }

        setMapModeSwitch(naverMap0)

        Thread(Runnable {
            onRequestPipe()
        }).start()
    }

    /**
     * Toolbar 에서 지도 모드 전환 스위치 구현
     *
     * @param naverMap API 를 호출하는 인터페이스 역할을 하는 NaverMapActivity 객체
     */
    private fun setMapModeSwitch(naverMap: NaverMap) {
        val toggleSwitch = findViewById<MaterialButtonToggleGroup>(R.id.nmap_mapmode_switch)
        toggleSwitch.apply {
            visibility = View.VISIBLE
            isSingleSelection = true
            val green = resources.getColor(R.color.green, null)
            val white = resources.getColor(android.R.color.white, null)
            addOnButtonCheckedListener { group, _, _ ->
                when (group.checkedButtonId) {
                    R.id.button_hybrid -> {
                        naverMap.mapType = NaverMap.MapType.Hybrid
                        group[0].setBackgroundColor(white)
                        group[1].setBackgroundColor(green)
                    }
                    R.id.button_basic -> {
                        naverMap.mapType = NaverMap.MapType.Basic
                        group[1].setBackgroundColor(white)
                        group[0].setBackgroundColor(green)
                    }
                }
            }
        }
    }

    private fun onRequestPipe() {
        val jsonQuery = JsonObject()
        val bounds = naverMap!!.contentBounds
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
                        val resId = parsePipeType(jsonObject["pipe"].asString).drawRes
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
                    if (spiLocationMap["origin_id"] == null) {
                        val latLng = naverMap!!.cameraPosition.target
                        spiLocationMap["latitude"] = latLng.latitude
                        spiLocationMap["longitude"] = latLng.longitude
                    }
                    setResult(
                        RESULT_OK,
                        Intent()
                            .putExtra("spiLocations", spiLocationMap)
                            .putExtra("pipeLocations", pipeLocationMap)
                    )
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
            TAG_SURVEY_PIPE -> {
                if (index == RESULT_PASS) {
                    AppPreference.defaultPrefs(this)["savedCheckedIndex"] = text[0]!!.toInt()
                    val x = text[1]!!.toDouble()
                    val y = text[2]!!.toDouble()
                    pipeLatLng = convertTmToLatLng(x, y)
                    pipeLocationMap["latitude"] = pipeLatLng.latitude
                    pipeLocationMap["longitude"] = pipeLatLng.longitude
                    pipeLocationMap["origin"] = originPoint.name
                    pipeLocationMap["coordinate_x"] = x
                    pipeLocationMap["coordinate_y"] = y
                    val pipeType = intent.getStringExtra("pipeType")!!
                    val resId = parsePipeType(pipeType).drawRes
                    val marker = Marker().apply {
                        position = pipeLatLng
                        captionText = "관로"
                        captionColor = Color.RED
                        captionHaloColor = Color.rgb(255, 255, 255)
                        captionTextSize = 16f
                        setCaptionAligns(Align.Top)
                        icon = OverlayImage.fromResource(resId)
                        isHideCollidedSymbols = true
                        zIndex = 100
                    }
                    marker.map = naverMap!!
                    val cameraUpdate = CameraUpdate
                        .scrollAndZoomTo(pipeLatLng, ZOOM_MAX - 1)
                        .finishCallback {
                            onRequestPipe()
                        }
                    naverMap!!.moveCamera(cameraUpdate)
                } else {
                    val cameraUpdate = CameraUpdate.zoomTo(ZOOM_DEFAULT)
                    naverMap!!.moveCamera(cameraUpdate)
                }
                val preferences = AppPreference.defaultPrefs(this)
                if (preferences["switch_location", false]!!) {
                    onSpiSurveyDialog()
                } else {
                    record_gps.visibility = View.VISIBLE
                }
            }
            TAG_SURVEY_SPI -> {
                if (index == RESULT_PASS) {
                    AppPreference.defaultPrefs(this)["savedCheckedIndex"] = text[0]!!.toInt()
                    val x = text[1]!!.toDouble()
                    val y = text[2]!!.toDouble()
                    spiLatLng = convertTmToLatLng(x, y)
                    spiLocationMap["latitude"] = spiLatLng.latitude
                    spiLocationMap["longitude"] = spiLatLng.longitude
                    spiLocationMap["origin"] = originPoint.name
                    spiLocationMap["coordinate_x"] = x
                    spiLocationMap["coordinate_y"] = y
                    val marker = Marker().apply {
                        position = spiLatLng
                        anchor = DEFAULT_ANCHOR
                        icon = OverlayImage.fromResource(R.drawable.ic_marker_3_2)
                        isHideCollidedSymbols = true
                        zIndex = 100
                    }
                    marker.map = naverMap!!
                    val cameraUpdate = CameraUpdate
                        .scrollAndZoomTo(spiLatLng, ZOOM_MAX - 2)
                        .finishCallback {
                            onRequestPipe()
                        }
                    naverMap!!.moveCamera(cameraUpdate)
                } else {
                    record_gps.visibility = View.VISIBLE
                    ic_marker_3.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * Tm 좌표계를 LatLng 경위도 좌표계로 변환한다. 반드시 변환 과정에서 X, Y 좌표를 반전시켜 리턴한다.
     */
    private fun convertTmToLatLng(Tm1: Double, Tm2: Double): LatLng {
        val surveyPoint = convert(originPoint, GeoTrans.Coordinate.GEO, GeoPoint(Tm2, Tm1))
        return LatLng(surveyPoint.y, surveyPoint.x)
    }

    private fun onPipeSurveyDialog() {
        Thread(Runnable {
            val bundle = Bundle()
            val checkedIndex: Int? = AppPreference.defaultPrefs(this)["savedCheckedIndex"]
            if (checkedIndex != null) {
                bundle.putInt("savedCheckedIndex", checkedIndex)
            } else {
                bundle.putInt("savedCheckedIndex", -1)
            }
            surveyDialog2.apply {
                arguments = bundle
                isCancelable = false
            }.show(supportFragmentManager, TAG_SURVEY_PIPE)
        }).start()
    }

    private fun onSpiSurveyDialog() {
        record_gps.visibility = View.GONE
        ic_marker_3.visibility = View.GONE
        Thread(Runnable {
            val bundle = Bundle()
            val checkedIndex: Int? = AppPreference.defaultPrefs(this)["savedCheckedIndex"]
            if (checkedIndex != null) {
                bundle.putInt("savedCheckedIndex", checkedIndex)
            } else {
                bundle.putInt("savedCheckedIndex", -1)
            }
            surveyDialog.apply {
                arguments = bundle
                isCancelable = false
            }.show(supportFragmentManager, TAG_SURVEY_SPI)
        }).start()
    }

    override fun onResume() {
        super.onResume()
        onResumeNfc()
    }

    override fun onPause() {
        super.onPause()
        onPauseNfc()
    }

    override fun onBackPressed() {
        if (surveyDialog.isAdded || surveyDialog2.isAdded) return
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
