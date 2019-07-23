package kr.djspi.pipe01

import android.content.Intent
import android.content.Intent.ACTION_DIAL
import android.graphics.drawable.GradientDrawable
import android.net.Uri.parse
import android.os.Bundle
import android.text.Html.fromHtml
import android.view.View
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_pipe_view.*
import kr.djspi.pipe01.dto.Entry
import kr.djspi.pipe01.dto.Entry.parseEntry
import kr.djspi.pipe01.dto.SpiPhotoObject
import kr.djspi.pipe01.tab.OnRecordListener
import kr.djspi.pipe01.tab.TabAdapter
import kr.djspi.pipe01.util.onNewIntentIgnore
import kr.djspi.pipe01.util.onPauseNfc
import kr.djspi.pipe01.util.onResumeNfc
import java.io.Serializable

class ViewActivity : BaseActivity(), Serializable, OnRecordListener {

    private var pipeIndex: Int = 0
    private var photoObject: SpiPhotoObject? = null
    private lateinit var previewEntries: ArrayList<Entry>
    private lateinit var jsonObject: JsonObject
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.let {
            val jsonString: String? = it.getStringExtra("PipeView")
            jsonObject = JsonParser().parse(jsonString).asJsonObject

            val preview = it.getSerializableExtra("RegisterPreview")
            val fHorizontal = it.getStringExtra("fHorizontal")
            val fVertical = it.getStringExtra("fVertical")
            pipeIndex = it.getIntExtra("PipeIndex", 0)
            if (preview is ArrayList<*>) {
                previewEntries = preview as ArrayList<Entry>
                jsonObject = parseEntry(previewEntries, pipeIndex, fHorizontal, fVertical)
            }

            val photo = it.getSerializableExtra("SpiPhotoObject")
            if (photo is SpiPhotoObject) {
                photoObject = photo
            }
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
        toolbar.title = "SPI ${jsonObject["pipe"].asString}"
        setTabLayout()
    }

    private fun setTabLayout() {
        if (previewEntries.isEmpty()) {
            tabs.removeTab(tabs.getTabAt(3))
        }
        viewPager = findViewById(R.id.container)
        viewPager.apply {
            adapter = TabAdapter(supportFragmentManager, tabs.tabCount)
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
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
        if (jsonObject["id"].isJsonNull) {
            txt_id.visibility = View.GONE
        } else {
            txt_id.text = fromHtml(getString(R.string.nfc_info_id, jsonObject["id"].asString))
        }
    }

    private fun setSuperviseInfo() {
        if (jsonObject["supervise"].isJsonNull) {
            txt_company.visibility = View.GONE
            txt_contact.visibility = View.GONE
        } else {
            txt_company.text =
                fromHtml(getString(R.string.info_company, jsonObject["supervise"].asString))
            txt_contact.text =
                fromHtml(getString(R.string.info_contact, jsonObject["supervise_contact"].asString))
            txt_contact.setOnClickListener {
                startActivity(
                    Intent(
                        ACTION_DIAL,
                        parse("tel:${jsonObject["supervise_contact"].asString}")
                    )
                )
            }
        }
    }

    private fun setConstructionInfo() {
        if (jsonObject["construction"].isJsonNull) {
            txt_construction.visibility = View.GONE
        } else {
            val construction = jsonObject["construction"].asString
            val constructionContact = jsonObject["construction_contact"].asString
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

    override fun getJsonObject(): JsonObject = jsonObject

    override fun getUri(): String? = photoObject?.uri

    override fun onRecord(tag: String?, result: Int) {
        when (result) {
            RESULT_PASS -> {
                startActivityForResult(
                    Intent(this, SpiLocationActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), REQUEST_MAP
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_MAP) {
                val locations = data.getDoubleArrayExtra("locations")
                val currentEntry = previewEntries[pipeIndex]
                val location = currentEntry.spi_location
                location.latitude = locations[0]
                location.longitude = locations[1]
                location.count = 0
                currentEntry.spi_location = location
                previewEntries[pipeIndex] = currentEntry
                startActivity(
                    Intent(this, SpiPostActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra("entry", previewEntries)
                        .putExtra("SpiPhotoObject", photoObject)
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        onNewIntentIgnore()
    }

    private inner class TabSelected : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            viewPager.currentItem = tab.position
            if (tab.position == 3) lay_bottom.visibility = View.GONE
            else lay_bottom.visibility = View.VISIBLE
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }
    }

    companion object {
        private const val REQUEST_MAP = 30001
        private const val RESULT_PASS = 200
        private const val RESULT_FAIL = 400
    }
}
