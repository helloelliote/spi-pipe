package kr.djspi.pipe01

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import androidx.core.view.get
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.gson.JsonObject
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.NaverMap.OnCameraChangeListener
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MapConstants
import com.naver.maps.map.util.MarkerIcons
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_spi_location.*
import kr.djspi.pipe01.AppPreference.get
import kr.djspi.pipe01.AppPreference.set
import kr.djspi.pipe01.BuildConfig.CLIENT_ID
import kr.djspi.pipe01.Const.RESULT_FAIL
import kr.djspi.pipe01.Const.RESULT_PASS
import kr.djspi.pipe01.Const.TAG_SURVEY
import kr.djspi.pipe01.Const.TAG_SURVEY_PIPE
import kr.djspi.pipe01.Const.TAG_SURVEY_SPI
import kr.djspi.pipe01.dto.PipeType.PipeTypeEnum.Companion.parsePipeType
import kr.djspi.pipe01.fragment.OnSelectListener
import kr.djspi.pipe01.fragment.SurveyDialog
import kr.djspi.pipe01.fragment.SurveyDialog2
import kr.djspi.pipe01.fragment.SurveyDialog3
import kr.djspi.pipe01.geolocation.GeoPoint
import kr.djspi.pipe01.geolocation.GeoTrans
import kr.djspi.pipe01.geolocation.GeoTrans.convert
import kr.djspi.pipe01.network.Retrofit2x
import kr.djspi.pipe01.util.*
import java.io.Serializable
import java.util.*

class SpiLocationActivity : LocationUpdate(), OnMapReadyCallback, OnClickListener, OnSelectListener, Serializable {

    private var surveyDialog: SurveyDialog = SurveyDialog()
    private var surveyDialog2: SurveyDialog2 = SurveyDialog2()
    private var surveyDialog3: SurveyDialog3 = SurveyDialog3()
    private var mapFragment: MapFragment? = null
    private val spiLocationMap = HashMap<String, Any?>(5)
    private val pipeLocationMap = HashMap<String, Any?>(5)
    private lateinit var naverMap: NaverMap
    private lateinit var spiLatLng: LatLng
    private lateinit var pipeLatLng: LatLng
    private lateinit var spiMarker: Marker
    private lateinit var pipeMarker: Marker
    private lateinit var cameraChangeListener: OnCameraChangeListener

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

        onSurveyDialog()
    }

    /**
     * 네이버 지도의 기초 UI 설정
     */
    @UiThread
    private fun setNaverMap() {
        mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment? ?: MapFragment.newInstance(
                NaverMapOptions().locale(Locale.KOREA)
                    .camera(CameraPosition(LatLng(currentLocation!!), ZOOM_DEFAULT, 0.0, 0.0))
                    .enabledLayerGroups(NaverMap.LAYER_GROUP_BUILDING).minZoom(ZOOM_MIN).maxZoom(ZOOM_MAX)
                    .extent(MapConstants.EXTENT_KOREA).compassEnabled(true).locationButtonEnabled(true)
                    .zoomGesturesEnabled(true)
            ).also {
                supportFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(naverMap0: NaverMap) {
        this.naverMap = naverMap0
        val fusedLocationSource = FusedLocationSource(this, 100)
        naverMap.apply {
            locationSource = fusedLocationSource
            locationTrackingMode = LocationTrackingMode.Follow
            addOnOptionChangeListener {
                val mode: LocationTrackingMode = this.locationTrackingMode
                fusedLocationSource.isCompassEnabled =
                    mode == LocationTrackingMode.Follow || mode == LocationTrackingMode.Face
            }
        }
        setMapModeSwitch(naverMap0)

        spiMarker = Marker().apply {
            captionColor = Color.RED
            captionHaloColor = Color.WHITE
            captionText = getString(R.string.marker_caption_location_spi)
            captionTextSize = 16f
            setCaptionAligns(Align.Top)
            icon = MarkerIcons.BLACK
            iconTintColor = Color.RED
            height = 80
            width = 60
            isHideCollidedMarkers = false
            isHideCollidedSymbols = true
        }
        cameraChangeListener = OnCameraChangeListener { _, _ ->
            spiMarker.position = naverMap.cameraPosition.target
            if (!btn_confirm.isEnabled) {
                btn_confirm.isEnabled = true
                btn_confirm.setBackgroundColor(getColor(R.color.colorPrimaryDark))
            }
        }

        pipeMarker = Marker().apply {
            captionColor = Color.BLUE
            captionHaloColor = Color.WHITE
            captionTextSize = 16f
            isHideCollidedMarkers = false
            isHideCollidedSymbols = true
        }

        Thread { onRequestPipe() }.start()
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
                    if (spiLocationMap["latitude"] == null || spiLocationMap["longitude"] == null) { // SPI 측량좌표가 입력되지 않음
                        if (!isSpiMarkerPositionValid()) { // SPI 측량좌표가 유효한 값인지를 확인
                            messageDialog(
                                13, "${intent.getStringExtra("hDirection")} ${intent.getStringExtra("vDirection")}"
                            )
                            (v as MaterialButton).apply {
                                isEnabled = false
                                setBackgroundColor(getColor(android.R.color.darker_gray))
                            }
                            return // SPI 측량좌표가 유효하지 않을 경우(=관로 이격거리가 존재함에도 SPI 설치지점을 관로 직상에 위치하여 등록하려는 경우) 입력 불가
                        }
                        val latLng = spiMarker.position.toLatLng()
                        spiLocationMap["latitude"] = latLng.latitude
                        spiLocationMap["longitude"] = latLng.longitude
                    }
                    setResult(
                        RESULT_OK,
                        Intent().putExtra("spiLocations", spiLocationMap).putExtra("pipeLocations", pipeLocationMap)
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
            TAG_SURVEY -> {
                if (index == RESULT_PASS) {
                    AppPreference.defaultPrefs(this)["savedCheckedIndex"] = text[0]!!.toInt()
                    val xPipe = text[1]!!.toDouble()
                    val yPipe = text[2]!!.toDouble()
                    val xSpi = text[3]!!.toDouble()
                    val ySpi = text[4]!!.toDouble()
                    pipeLatLng = convertTmToLatLng(xPipe, yPipe)
                    spiLatLng = convertTmToLatLng(xSpi, ySpi)
                    pipeLocationMap["latitude"] = pipeLatLng.latitude
                    pipeLocationMap["longitude"] = pipeLatLng.longitude
                    pipeLocationMap["origin"] = originPoint.name
                    pipeLocationMap["coordinate_x"] = xPipe
                    pipeLocationMap["coordinate_y"] = yPipe
                    spiLocationMap["latitude"] = spiLatLng.latitude
                    spiLocationMap["longitude"] = spiLatLng.longitude
                    spiLocationMap["origin"] = originPoint.name
                    spiLocationMap["coordinate_x"] = xSpi
                    spiLocationMap["coordinate_y"] = ySpi
                    val pipeType = intent.getStringExtra("pipeType")!!
                    val resId = parsePipeType(pipeType).drawRes
                    addPipeMarker(pipeLatLng, resId, pipeType)
                    addSpiMarker(spiLatLng).run {
                        this.setMarkerAddress(spiLatLng.longitude, spiLatLng.latitude)
                    }
                } else if (index == RESULT_FAIL) {
                    record_gps.visibility = View.VISIBLE
                    naverMap.apply {
                        addOnCameraChangeListener(cameraChangeListener)
                        minZoom = ZOOM_MAX - 2
                        maxZoom = ZOOM_MAX
                    }
                    spiMarker.apply {
                        position = naverMap.cameraPosition.target
                        map = naverMap
                    }
                }
            }
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
                    addPipeMarker(pipeLatLng, resId, pipeType).run {
                        this.setMarkerAddress(pipeLatLng.longitude, pipeLatLng.latitude)
                    }

                    naverMap.apply {
                        addOnCameraChangeListener(cameraChangeListener)
                        minZoom = ZOOM_MAX - 2
                        maxZoom = ZOOM_MAX
                    }.moveCamera(
                        CameraUpdate.scrollAndZoomTo(pipeLatLng, ZOOM_MAX - 2).finishCallback { onRequestPipe() })

                    record_gps.visibility = View.VISIBLE
                    spiMarker.apply {
                        captionColor = Color.BLUE
                        iconTintColor = Color.BLUE
                        position = pipeLatLng
                        map = naverMap
                    }
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
                    addSpiMarker(spiLatLng).run {
                        this.setMarkerAddress(spiLatLng.longitude, spiLatLng.latitude)
                    }
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

    private fun isSpiMarkerPositionValid(): Boolean {
        return when {
            pipeLocationMap["latitude"] != null || pipeLocationMap["longitude"] != null -> { // 관로 측량좌표가 입력됨
                val horizontal = intent.getDoubleExtra("horizontal", 0.0)
                val vertical = intent.getDoubleExtra("vertical", 0.0)
                if (vertical > 0 || horizontal > 0) { // 사용자 입력값에 관로 이격거리가 존재함
                    spiMarker.position != pipeMarker.position // 관로 측량좌표가 입력되었고, 이격거리가 존재함에도 SPI 설치지점 마커가 관로위치 좌표 직상에 그대로 위치할 경우 false 리턴
                } else true
            }
            else -> true
        }
    }

    private fun addPipeMarker(latLng: LatLng, @DrawableRes resId: Int, pipeType: String): Marker {
        return pipeMarker.apply {
            position = latLng
            icon = OverlayImage.fromResource(resId)
            captionText = parsePipeType(pipeType).pipeName
            map = naverMap
        }
    }

    private fun addSpiMarker(latLng: LatLng): Marker {
        naverMap.apply {
            removeOnCameraChangeListener(cameraChangeListener)
        }.moveCamera(CameraUpdate.scrollAndZoomTo(latLng, ZOOM_MAX - 2).finishCallback { onRequestPipe() })

        return spiMarker.apply {
            position = latLng
            map = naverMap
        }
    }

    private fun Marker.setMarkerAddress(x: Double, y: Double) {
        val marker = this
        Retrofit2x.coord2Address(x, y).enqueue(object : RetrofitCallback() {
            override fun onResponse(response: JsonObject) {
                val metaData = response["meta"].asJsonObject
                val totalCount = metaData["total_count"].asInt
                if (totalCount == 0) {
                    marker.captionColor = Color.MAGENTA
                    marker.captionText = getString(R.string.marker_caption_location_invalid)
                    marker.subCaptionText = getString(R.string.marker_caption_sub_location_invalid)
                    btn_confirm.apply {
                        setBackgroundColor(getColor(android.R.color.darker_gray))
                    }
                    return
                }
                val documents = response["documents"].asJsonArray[0].asJsonObject
                val address = documents["address"].asJsonObject
                val addressName = address["address_name"]
                marker.subCaptionText = addressName.asString
                btn_confirm.apply {
                    setBackgroundColor(getColor(R.color.colorPrimaryDark))
                }
            }

            override fun onFailure(throwable: Throwable) {
                marker.captionColor = Color.MAGENTA
                marker.captionText = getString(R.string.marker_caption_network_invalid)
                marker.subCaptionText = getString(R.string.marker_caption_sub_network_invalid)
            }
        })
    }

    private fun onSurveyDialog() {
        record_gps.visibility = View.INVISIBLE
        Thread {
            val bundle = Bundle()
            val checkedIndex: Int? = AppPreference.defaultPrefs(this)["savedCheckedIndex"]
            if (checkedIndex != null) {
                bundle.putInt("savedCheckedIndex", checkedIndex)
            } else {
                bundle.putInt("savedCheckedIndex", -1)
            }
            surveyDialog3.apply {
                arguments = bundle
                isCancelable = false
            }.show(supportFragmentManager, TAG_SURVEY)
        }.start()
    }

    private fun onPipeSurveyDialog() {
        Thread {
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
        }.start()
    }

    private fun onSpiSurveyDialog() {
        record_gps.visibility = View.GONE
        Thread {
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
        }.start()
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
        if (surveyDialog.isAdded || surveyDialog2.isAdded || surveyDialog3.isAdded) return
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
        lateinit var originPoint: GeoTrans.Coordinate
    }
}
