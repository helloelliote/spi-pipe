package kr.djspi.pipe01

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SearchView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.gson.JsonObject
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.NaverMap.LAYER_GROUP_BUILDING
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MapConstants.EXTENT_KOREA
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_navermap.*
import kr.djspi.pipe01.BuildConfig.CLIENT_ID
import kr.djspi.pipe01.util.RetrofitCallback
import kr.djspi.pipe01.util.messageDialog
import kr.djspi.pipe01.util.retrofit2x
import java.io.Serializable
import java.util.*

class NaverMapActivity : LocationUpdate(), OnMapReadyCallback, Serializable {

    private val ZOOM_DEFAULT = 18.0 // 기본 줌레벨
    private val ZOOM_MIN = 6.0 // 최소 줌레벨
    private val ZOOM_GET = 12.0
    private val ZOOM_MAX = NaverMap.MAXIMUM_ZOOM.toDouble() // 최대 줌레벨(21)
    private var fusedLocationSource: FusedLocationSource? = null
    private val overlayOnclickListener = Overlay.OnClickListener { overlay ->
        when (overlay) {
            is Marker -> {
//                if (overlay.infoWindow == null) overlay.infoWindow?.open(overlay) else overlay.infoWindow?.close()
                overlay.infoWindow?.open(overlay)
            }
            is InfoWindow -> {
                val jsonObject = overlay.marker?.tag as JsonObject
                startActivity(
                    Intent(this, ViewActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra("PipeView", jsonObject.toString())
                )
            }
        }
        return@OnClickListener true
    }
    private val placesArrayList = ArrayList<HashMap<String, String>>(5)
    lateinit var behavior: BottomSheetBehavior<*>
    lateinit var placesListAdapter: SetTopSheet.ListViewAdapter

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
        fusedLocationSource = FusedLocationSource(this, 100)
        naverMap.apply {
            locationSource = fusedLocationSource
            locationTrackingMode = LocationTrackingMode.Follow
            addOnOptionChangeListener {
                val mode: LocationTrackingMode = naverMap.locationTrackingMode
                fusedLocationSource?.isCompassEnabled =
                    mode == LocationTrackingMode.Follow || mode == LocationTrackingMode.Face
            }
            setMapModeSwitch(naverMap)
            setOverlayListener()
        }
    }

    /**
     * Toolbar 에서 지도 모드 전환 스위치 구현
     *
     * @param naverMap API 를 호출하는 인터페이스 역할을 하는 NaverMapActivity 객체
     */
    private fun setMapModeSwitch(naverMap: NaverMap) {
        val toggleSwitch = findViewById<ToggleSwitch>(R.id.nmap_mapmode_switch)
        toggleSwitch.apply {
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
            onClickListener = this@NaverMapActivity.overlayOnclickListener
        }
    }

    inner class SetTopSheet(naverMap: NaverMap) {

        private val searchView: SearchView

        init {
            placesListAdapter = ListViewAdapter(applicationContext, naverMap, placesArrayList)
            nmap_listView.adapter = placesListAdapter
            setDefaultKeyMode(Activity.DEFAULT_KEYS_SEARCH_LOCAL)
            searchView = findViewById<SearchView>(R.id.nmap_searchView).apply {
                isSubmitButtonEnabled = true
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
            retrofit2x.searchPlaces(query, coordinate).enqueue(object : RetrofitCallback() {
                override fun onResponses(response: JsonObject?) {
                    placesArrayList.clear()
                    behavior.state = STATE_COLLAPSED
                    response?.let {
                        val places = it["places"].asJsonArray
                        if (places == null || places.size() == 0) {
                            messageDialog(0, getString(R.string.popup_error_noplace))
                            return
                        }
                        places.forEach { place ->
                            val obj = place.asJsonObject
                            val hashMap = HashMap<String, String>(3)
                            hashMap["name"] = obj["name"].asString
                            hashMap["x"] = obj["x"].asString
                            hashMap["y"] = obj["y"].asString
                            placesArrayList.add(hashMap)
                        }
                    }
                    placesListAdapter.notifyDataSetChanged()
                }

                override fun onFailures(throwable: Throwable) {
                    messageDialog(8, throwable.message ?: "")
                }
            })
        }

        inner class ListViewAdapter(
            context: Context,
            val naverMap: NaverMap,
            private val placesArrayList: ArrayList<HashMap<String, String>>
        ) : BaseAdapter() {

            private var inflater = LayoutInflater.from(context)

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
                holder.name?.text = hashMap["name"]
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
                var name: TextView? = null
            }
        }
    }
}