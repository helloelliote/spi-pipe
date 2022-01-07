package kr.djspi.pipe01

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.style.TextAppearanceSpan
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.naver.maps.map.NaverMapSdk
import kotlinx.android.synthetic.main.activity_base.*
import kr.djspi.pipe01.nfc.NfcUtil
import kr.djspi.pipe01.sql.SuperviseDatabase
import kr.djspi.pipe01.util.messageDialog
import kr.djspi.pipe01.util.screenScale
import kr.djspi.pipe01.util.settingsMenuEnabled

open class BaseActivity : AppCompatActivity(), OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    lateinit var nmapFind: TextView
    lateinit var nfcUtil: NfcUtil
    var locationFailureCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcUtil = NfcUtil(this, javaClass)
        defPackage = this@BaseActivity.packageName
        if (screenRatio == 0.0f) {
            screenRatio = this@BaseActivity.screenScale()
        }
    }

    @SuppressLint("InflateParams")
    override fun setContentView(layoutResID: Int) {
        val view = layoutInflater.inflate(R.layout.activity_base, null)
        val activityContainer = view.findViewById<FrameLayout>(R.id.activity_content)
        layoutInflater.inflate(layoutResID, activityContainer, true)
        super.setContentView(view)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawer = findViewById(R.id.drawer_layout)
        setNavigationBarDrawer()
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)
        toolbar?.setTitleTextAppearance(this, R.style.TitleHeader)
        nmapFind = findViewById(R.id.nmap_find)
        nmapFind.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                when {
                    currentLocation != null -> {
                        locationFailureCount = 0
                        startActivity(
                            Intent(context, NaverMapActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        )
                    }
                    else -> {
                        runLocationCounter(this@BaseActivity)
                    }
                }
            }
        }
    }

    private fun setNavigationBarDrawer() {
        Thread(Runnable {
            ActionBarDrawerToggle(
                this@BaseActivity,
                drawer,
                toolbar,
                R.string.nav_drawer_open,
                R.string.nav_drawer_close
            ).apply {
                isDrawerIndicatorEnabled = false
                setHomeAsUpIndicator(R.drawable.ic_drawer)
                setToolbarNavigationClickListener {
                    drawer.openDrawer(GravityCompat.START)
                }
                drawer.addDrawerListener(this)
                syncState()
            }

            val navigationView = drawer.findViewById<NavigationView>(R.id.navView)
            val menu = navigationView.menu
            val headerView = navigationView.getHeaderView(0)
            navigationView.setNavigationItemSelectedListener(this@BaseActivity)

            val version = headerView.findViewById<TextView>(R.id.versionName)
            val buildType: String = if (BuildConfig.BUILD_TYPE == "debug") "(DEBUG)" else ""
            version.text = getString(
                R.string.nav_version_name,
                BuildConfig.VERSION_NAME,
                buildType
            )
            val email = headerView.findViewById<TextView>(R.id.contact_email)
            email.text = getString(R.string.nav_email, getString(R.string.nav_dj_email))
            val phone = headerView.findViewById<TextView>(R.id.contact_phone)
            phone.text = getString(R.string.nav_dj_phone)
            val what: Any = TextAppearanceSpan(this@BaseActivity, R.style.TextAppearance20sp)

            val titleApp = menu.findItem(R.id.nav_apptitle)
            val spannable = SpannableString(titleApp.title)
            spannable.setSpan(what, 0, spannable.length, 0)
            titleApp.title = spannable

            val titleSetting = menu.findItem(R.id.nav_settingtitle)
            val spannable2 = SpannableString(titleSetting.title)
            spannable2.setSpan(what, 0, spannable2.length, 0)
            titleSetting.title = spannable2

            menu.findItem(R.id.nav_settingtitle).isVisible = settingsMenuEnabled()

            nav_close.setOnClickListener { drawer.closeDrawer(GravityCompat.START) }
        }).start()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer.closeDrawer(GravityCompat.START, false)
        when (item.itemId) {
            R.id.nav_guide -> {
                AlertDialog.Builder(this)
                    .setTitle("SPI 정보")
                    .setMessage(getString(R.string.nav_guide_content))
                    .setPositiveButton("닫기") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .also {
                        it.findViewById<TextView>(android.R.id.message)?.textSize = 14.0f
                        it.show()
                    }
            }
            R.id.nav_manual_video -> {
                val appIntent = Intent(
                    Intent.ACTION_VIEW, Uri.parse("vnd.youtube:6Ttio_ff3n8")
                )
                val webIntent = Intent(
                    Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=6Ttio_ff3n8")
                )
                try {
                    startActivity(appIntent)
                } catch (e: ActivityNotFoundException) {
                    startActivity(webIntent)
                }
            }
            R.id.nav_homepage -> startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.nav_dj_homepage))
                )
            )
            R.id.nav_settings -> startActivity(
                Intent(this, SettingsActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    fun runLocationCounter(context: Context) {
        when {
            locationFailureCount == 1 -> {
                NaverMapSdk.getInstance(context).flushCache {}
            }
            locationFailureCount >= 2 -> {
                locationFailureCount++
                progressbar.visibility = View.INVISIBLE
                messageDialog(10, getString(R.string.popup_fail_location), false)
                return
            }
        }
        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                locationFailureCount++
                progressbar.visibility = View.INVISIBLE
                messageDialog(0, getString(R.string.popup_error_location))
            }
        }.start()
    }

    companion object {
        lateinit var defPackage: String
        lateinit var currentSerial: String
        var currentLocation: Location? = null
        var superviseDb: SuperviseDatabase? = null
        var screenRatio: Float = 0.0f
        var isReadyForPost: Boolean =
            false // (SpiPostActivity.class) 원치 않은 시점에서 태깅 동작이 발생하지 않도록 한다.
    }
}
