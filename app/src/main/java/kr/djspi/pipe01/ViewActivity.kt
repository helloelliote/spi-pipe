package kr.djspi.pipe01

import android.graphics.drawable.GradientDrawable
import android.net.Uri
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
import java.io.Serializable

class ViewActivity : BaseActivity(), Serializable, OnRecordListener {

    private var pipeIndex: Int = 0
    private lateinit var previewEntries: ArrayList<Entry>
    private lateinit var jsonObject: JsonObject
    private lateinit var photoObject: SpiPhotoObject
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
        if ((jsonObject["id"].asString).isNullOrBlank()) {
            txt_id.visibility = View.GONE
        } else {
            txt_id.text = fromHtml(getString(R.string.nfc_info_id, jsonObject["id"].asString))
        }
    }

    private fun setSuperviseInfo() {

    }

    private fun setConstructionInfo() {

    }

    override fun getJsonObject(): JsonObject {

    }

    override fun getUri(): Uri {

    }

    override fun onRecord(tag: String?, result: Int) {

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
}