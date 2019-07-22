package kr.djspi.pipe01

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import android.widget.TextView
import androidx.transition.Transition
import androidx.transition.TransitionManager.beginDelayedTransition
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.gson.JsonObject
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.NaverMap.LAYER_GROUP_BUILDING
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay.OnClickListener
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MapConstants.EXTENT_KOREA
import com.transitionseverywhere.ChangeText
import com.transitionseverywhere.ChangeText.CHANGE_BEHAVIOR_OUT_IN
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_navermap.*
import kr.djspi.pipe01.BuildConfig.CLIENT_ID
import kr.djspi.pipe01.dto.PipeType.parsePipeType
import kr.djspi.pipe01.network.Retrofit2x
import kr.djspi.pipe01.util.*
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class NaverMapActivity : LocationUpdate(), OnMapReadyCallback, Serializable {

    private val placesArrayList = ArrayList<HashMap<String, String>>(5)
    private lateinit var overlayOnclickListener: OnClickListener
    private lateinit var behavior: BottomSheetBehavior<*>
    private lateinit var placesListAdapter: SetTopSheet.ListViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // https://console.ncloud.com/mc/solution/naverService/application 에서 클라이언트 ID 발급
        NaverMapSdk.getInstance(this).client = NaverMapSdk.NaverCloudPlatformClient(CLIENT_ID)
        setContentView(R.layout.activity_navermap)
        setNaverMap()
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        toolbar.visibility = View.GONE
    }

    private fun setNaverMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance(
                NaverMapOptions()
                    .locale(Locale.KOREA)
                    .contentPadding(0, 45, 0, 45)
                    .camera(CameraPosition(LatLng(currentLocation!!), ZOOM_DEFAULT, 0.0, 0.0))
                    .enabledLayerGroups(LAYER_GROUP_BUILDING)
                    .minZoom(ZOOM_MIN)
                    .maxZoom(ZOOM_MAX)
                    .extent(EXTENT_KOREA)
                    .compassEnabled(true)
                    .locationButtonEnabled(true)
                    .zoomGesturesEnabled(true)
            ).also {
                supportFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(naverMap: NaverMap) {
        val fusedLocationSource = FusedLocationSource(this, 100)
        naverMap.apply {
            locationSource = fusedLocationSource
            locationTrackingMode = LocationTrackingMode.Follow
            addOnOptionChangeListener {
                val mode: LocationTrackingMode = naverMap.locationTrackingMode
                fusedLocationSource.isCompassEnabled =
                    mode == LocationTrackingMode.Follow || mode == LocationTrackingMode.Face
            }
        }
        setMapModeSwitch(naverMap)
        setOverlayListener()
        SetTopSheet(naverMap)
        SetBottomSheet(naverMap)
        onRequestPipe(naverMap)
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

    private fun setOverlayListener() {
        overlayOnclickListener = OnClickListener {
            when (it) {
                is Marker -> {
                    if (it.infoWindow == null) {
                        it.infoWindow?.open(it)
                    } else {
                        it.infoWindow?.close()
                    }
                }
                is InfoWindow -> {
                    it.marker?.let { marker ->
                        val jsonObject = marker.tag as JsonObject
                        startActivity(
                            Intent(this@NaverMapActivity, ViewActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                .putExtra("PipeView", jsonObject.toString())
                        )
                    }
                    it.close()
                }
            }
            return@OnClickListener true
        }

        InfoWindow(object : InfoWindow.DefaultTextAdapter(applicationContext) {
            override fun getText(infoWindow: InfoWindow): CharSequence {
                infoWindow.marker?.let {
                    val jsonObject = it.tag as JsonObject
                    val pipe = jsonObject["pipe"].asString
                    val id = jsonObject["id"].asString
                    return "$pipe $id"
                }
                return "ERROR"
            }
        }).apply {
            maxZoom = ZOOM_MAX
            minZoom = 14.0
            alpha = 0.85f
            onClickListener = overlayOnclickListener
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
                if (response["total_count"].asInt == 0) {
                    behavior.state = STATE_COLLAPSED
                    messageDialog(0, "표시할 SPI 정보가 없습니다")
                } else {
                    response["data"].asJsonArray.forEach { element ->
                        val jsonObject = element.asJsonObject
                        val lat = jsonObject["spi_latitude"].asDouble
                        val lng = jsonObject["spi_longitude"].asDouble
                        val resId = parsePipeType(jsonObject["pipe"].asString).drawRes
                        Marker(LatLng(lat, lng), OverlayImage.fromResource(resId)).apply {
                            tag = jsonObject
                            minZoom = ZOOM_GET
                            maxZoom = ZOOM_MAX
                            onClickListener = overlayOnclickListener
                        }.run {
                            this.map = naverMap
                        }
                    }
                }

            }

            override fun onFailure(throwable: Throwable) {
                messageDialog(8, throwable.message)
            }
        })
    }

    override fun onBackPressed() {
        if (behavior.state == STATE_EXPANDED) {
            behavior.state = STATE_COLLAPSED
            return
        } else if (placesArrayList.size != 0) {
            placesArrayList.clear()
            placesListAdapter.notifyDataSetChanged()
            return
        }
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        onResumeNfc()
    }

    override fun onPause() {
        super.onPause()
        onPauseNfc()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(null)
        onNewIntentIgnore()
    }

    private inner class SetTopSheet(naverMap: NaverMap) {

        private val searchView: SearchView

        init {
            placesListAdapter = ListViewAdapter(naverMap, placesArrayList)
            searchView = findViewById(R.id.nmap_searchView)
            setContentView()
        }

        private fun setContentView() {
            nmap_listView.adapter = placesListAdapter
            setDefaultKeyMode(Activity.DEFAULT_KEYS_SEARCH_LOCAL)
            searchView.apply {
                isSubmitButtonEnabled = true
                setOnQueryTextListener(object : OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        query?.let { setSearchPlaces(it) }
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        return false
                    }
                })
            }
        }

        private fun setSearchPlaces(query: String) {
            val latLng = LatLng(currentLocation!!)
            val coordinate = "${latLng.longitude},${latLng.latitude}"
            Retrofit2x.searchPlaces(query, coordinate).enqueue(object : RetrofitCallback() {
                override fun onResponse(response: JsonObject) {
                    placesArrayList.clear()
                    behavior.state = STATE_COLLAPSED
                    val places = response["places"].asJsonArray
                    if (places == null || places.size() == 0) {
                        messageDialog(0, getString(R.string.popup_error_noplace))
                        return
                    }
                    places.forEach { place ->
                        val jsonObject = place.asJsonObject
                        val hashMap = HashMap<String, String>(3)
                        hashMap["name"] = jsonObject["name"].asString
                        hashMap["x"] = jsonObject["x"].asString
                        hashMap["y"] = jsonObject["y"].asString
                        placesArrayList.add(hashMap)
                    }
                    placesListAdapter.notifyDataSetChanged()
                }

                override fun onFailure(throwable: Throwable) {
                    messageDialog(8, throwable.message)
                }
            })
        }

        inner class ListViewAdapter(
            private val naverMap: NaverMap,
            private val placesArrayList: ArrayList<HashMap<String, String>>,
            context: Context = applicationContext
        ) : BaseAdapter() {

            private val inflater: LayoutInflater = LayoutInflater.from(context)

            @SuppressLint("InflateParams")
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var view = convertView
                val holder: ItemHolder
                if (view == null) {
                    view = inflater.inflate(R.layout.list_searchplaces, null)
                    holder = ItemHolder()
                    holder.name = view.findViewById(R.id.name)
                    view.tag = holder
                } else {
                    holder = view.tag as ItemHolder
                }
                val hashMap = placesArrayList[position]
                holder.name.text = hashMap["name"]
                view!!.setOnClickListener {
                    searchView.setQuery(hashMap["name"], false)
                    val cordX = hashMap["x"]!!.toDouble()
                    val cordY = hashMap["y"]!!.toDouble()
                    val zoom = naverMap.cameraPosition.zoom
                    naverMap.moveCamera(CameraUpdate
                        .scrollAndZoomTo(LatLng(cordY, cordX), zoom)
                        .animate(CameraAnimation.Fly)
                        .finishCallback {
                            behavior.state = STATE_EXPANDED
                            searchView.clearFocus()
                        })
                }
                placesArrayList.clear()
                placesListAdapter.notifyDataSetChanged()
                return view
            }

            override fun getItem(position: Int): Any = placesArrayList[position]

            override fun getItemId(position: Int): Long = position.toLong()

            override fun getCount(): Int = placesArrayList.size

            private inner class ItemHolder {
                lateinit var name: TextView
            }
        }
    }

    private inner class SetBottomSheet(val naverMap: NaverMap) {

        private val transition: Transition

        init {
            transition = ChangeText().setChangeBehavior(CHANGE_BEHAVIOR_OUT_IN)
            behavior = from(nmap_bottom_sheet)
            setContentView()
        }

        private fun setContentView() {
            nmap_bottom_sheet_text.setOnClickListener {
                when (behavior.state) {
                    STATE_EXPANDED -> {
                        behavior.state = STATE_COLLAPSED
                    }
                    STATE_COLLAPSED -> {
                        if (naverMap.cameraPosition.zoom < ZOOM_GET) {
                            messageDialog(0, getString(R.string.popup_error_zoom))
                            return@setOnClickListener
                        }
                        behavior.state = STATE_EXPANDED
                    }
                }
            }
            val textExpanded = getString(R.string.map_search_input)
            val textCollapsed = getString(R.string.map_search_point)
            val bottomSheetHeight = nmap_bottom_sheet.height
            behavior.setBottomSheetCallback(object : BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    beginDelayedTransition(nmap_bottom_sheet, transition)
                    when (newState) {
                        STATE_EXPANDED -> {
                            nmap_bottom_sheet_text.text = textExpanded
                            naverMap.setContentPadding(0, 45, 0, bottomSheetHeight)
                            onRequestPipe(naverMap)
                        }
                        STATE_COLLAPSED -> {
                            nmap_bottom_sheet_text.text = textCollapsed
                            naverMap.setContentPadding(0, 45, 0, 45)
                            clearMarker()
                        }
                        else -> {
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            })
        }

        private fun clearMarker() {
            naverMap.pickAll(POINT_F, screenSize()).forEach {
                if (it is Marker) it.map = null
            }
        }
    }

    companion object {
        private const val ZOOM_DEFAULT = 18.0 // 기본 줌레벨
        private const val ZOOM_MIN = 6.0 // 최소 줌레벨
        private const val ZOOM_GET = 12.0
        private const val ZOOM_MAX = NaverMap.MAXIMUM_ZOOM.toDouble() // 최대 줌레벨(21)
        private val POINT_F = PointF(0.5f, 0.5f)
    }
}