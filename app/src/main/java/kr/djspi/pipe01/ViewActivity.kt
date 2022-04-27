package kr.djspi.pipe01

import android.content.Intent
import android.content.Intent.ACTION_DIAL
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.net.Uri.parse
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.gson.JsonObject
import com.google.gson.JsonParser.parseString
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_pipe_view.*
import kr.djspi.pipe01.Const.REQUEST_MAP
import kr.djspi.pipe01.Const.RESULT_PASS
import kr.djspi.pipe01.dto.Entry
import kr.djspi.pipe01.dto.Entry.Companion.parseEntry
import kr.djspi.pipe01.dto.SpiPhotoObject
import kr.djspi.pipe01.tab.InfoTab
import kr.djspi.pipe01.tab.OnRecordListener
import kr.djspi.pipe01.tab.TabAdapter
import kr.djspi.pipe01.util.fromHtml
import kr.djspi.pipe01.util.onNewIntentIgnore
import kr.djspi.pipe01.util.onPauseNfc
import kr.djspi.pipe01.util.onResumeNfc
import java.io.Serializable

class ViewActivity : BaseActivity(), Serializable, OnRecordListener {

    private var pipeIndex: Int = 0
    private var photoObject: SpiPhotoObject? = null
    private var previewEntries: ArrayList<Entry>? = null
    private var viewPager: ViewPager? = null
    private lateinit var jsonObj: JsonObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.let {
            val jsonString = intent.getStringExtra("PipeView")
            if (jsonString != "Register") {
                jsonObj = parseString(jsonString).asJsonObject
            }

            val preview = it.getSerializableExtra("RegisterPreview")
            val fHorizontal = it.getStringExtra("fHorizontal")
            val fVertical = it.getStringExtra("fVertical")
            pipeIndex = it.getIntExtra("PipeIndex", 0)
            if (preview is ArrayList<*>) {
                previewEntries = preview as ArrayList<Entry>
                jsonObj = parseEntry(previewEntries!!, pipeIndex, fHorizontal, fVertical)
            }
        }

        val classSerializable = intent.getSerializableExtra("PhotoObj")
        if (classSerializable != null) {
            photoObject = classSerializable as SpiPhotoObject
        }

        setContentView(R.layout.activity_pipe_view)

        runOnUiThread {
            setSpiIdInfo()
            setSuperviseInfo()
            setConstructionInfo()
        }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        toolbar.title = "SPI ${jsonObj["pipe"].asString}"
        setTabLayout()
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)
        super.nmapFind.text = getString(R.string.btn_current_spi)
        super.nmapFind.apply {
            setOnClickListener {
                startActivity(
                    Intent(context, NaverMapActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra("isSpiLocation", true)
                        .putExtra("spi_latitude", jsonObj["spi_latitude"].asDouble)
                        .putExtra("spi_longitude", jsonObj["spi_longitude"].asDouble)
                )
            }
        }
    }

    private fun setTabLayout() {
        if (previewEntries == null) {
            tabs.removeTab(tabs.getTabAt(3)!!)
        }
        viewPager = findViewById(R.id.container)
        viewPager?.let {
            it.adapter = TabAdapter(supportFragmentManager, tabs.tabCount)
            it.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        }
        tabs.addOnTabSelectedListener(TabSelected())
        val linearLayout: LinearLayout = tabs.getChildAt(0) as LinearLayout
        linearLayout.apply {
            showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            dividerDrawable = GradientDrawable().apply {
                setColor(resources.getColor(R.color.yellow, null))
                setSize(8, 1)
            }
        }
    }

    private fun setSpiIdInfo() {
        if (jsonObj["id"] == null) {
            txt_id.visibility = View.GONE
        } else {
            txt_id.text = fromHtml(getString(R.string.nfc_info_id, jsonObj["id"].asString))
        }
    }

    private fun setSuperviseInfo() {
        if (jsonObj["supervise"] == null) {
            txt_company.visibility = View.GONE
            txt_contact.visibility = View.GONE
        } else {
            txt_company.text =
                fromHtml(getString(R.string.info_company, jsonObj["supervise"].asString))
            txt_contact.text =
                fromHtml(getString(R.string.info_contact, jsonObj["supervise_contact"].asString))
            txt_contact.setOnClickListener {
                startActivity(
                    Intent(
                        ACTION_DIAL,
                        parse("tel:${jsonObj["supervise_contact"].asString}")
                    )
                )
            }
        }
    }

    private fun setConstructionInfo() {
        if (jsonObj["construction"] == null) {
            txt_construction.visibility = View.GONE
        } else {
            val construction = jsonObj["construction"].asString
            val constructionContact = jsonObj["construction_contact"].asString
            if (construction.isNotEmpty() || constructionContact.isNotEmpty()) {
                txt_construction.visibility = View.VISIBLE
                txt_construction.text = fromHtml(
                    getString(
                        R.string.nfc_info_construction,
                        construction,
                        constructionContact
                    )
                )
                if (constructionContact.isNotEmpty()) {
                    txt_construction.setOnClickListener {
                        startActivity(Intent(ACTION_DIAL, parse("tel:$constructionContact")))
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_MAP) {
//                val locations: DoubleArray = data.getDoubleArrayExtra("locations")!!
//                spiLocation?.latitude = locations[0]
//                spiLocation?.longitude = locations[1]

                val spiLocations =
                    data.getSerializableExtra("spiLocations") as HashMap<*, *>
                val pipeLocations =
                    data.getSerializableExtra("pipeLocations") as HashMap<*, *>
                val currentEntry = previewEntries!![pipeIndex]

                if (spiLocations.isNotEmpty()) {
                    val spiLocation = currentEntry.spi_location
                    spiLocation?.latitude = spiLocations["latitude"] as Double?
                    spiLocation?.longitude = spiLocations["longitude"] as Double?
                    spiLocation?.coordinate_x = spiLocations["coordinate_x"] as Double?
                    spiLocation?.coordinate_y = spiLocations["coordinate_y"] as Double?
                    spiLocation?.origin = spiLocations["origin"] as String?
                    spiLocation?.count = 0
                    currentEntry.spi_location = spiLocation
                }

                if (pipeLocations.isNotEmpty()) {
                    val pipeLocation = currentEntry.pipe_location
                    pipeLocation?.latitude = pipeLocations["latitude"] as Double?
                    pipeLocation?.longitude = pipeLocations["longitude"] as Double?
                    pipeLocation?.coordinate_x = pipeLocations["coordinate_x"] as Double?
                    pipeLocation?.coordinate_y = pipeLocations["coordinate_y"] as Double?
                    pipeLocation?.origin = pipeLocations["origin"] as String?
                    pipeLocation?.count = 0
                    currentEntry.pipe_location = pipeLocation
                }

                previewEntries!![pipeIndex] = currentEntry
                startActivity(
                    Intent(this, SpiPostActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra("entry", previewEntries)
                        .putExtra("Photos", photoObject)
                )
            }
        }
    }

    override val jsonObject: JsonObject
        get() = jsonObj
    override val uri: Uri?
        get() = if (photoObject == null) null else photoObject!!.getUri()


    override fun onRecord(tag: String, result: Int) {
        when (result) {
            RESULT_PASS -> {
                this.startActivityForResult(
                    Intent(this, SpiLocationActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra("pipeType", jsonObj["pipe"].asString)
                        .putExtra("horizontal", jsonObj["horizontal"].asDouble)
                        .putExtra("vertical", jsonObj["vertical"].asDouble)
                        .putExtra("hDirection", InfoTab.getHorizontalDirection(jsonObj))
                        .putExtra("vDirection", InfoTab.getVerticalDirection(jsonObj)), REQUEST_MAP
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        onResumeNfc()
    }

    override fun onPause() {
        super.onPause()
        onPauseNfc()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewPager = null
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        onNewIntentIgnore()
    }

    private inner class TabSelected : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            viewPager?.currentItem = tab.position
            if (tab.position == 3) lay_bottom.visibility = View.GONE
            else lay_bottom.visibility = View.VISIBLE
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }
    }
}
