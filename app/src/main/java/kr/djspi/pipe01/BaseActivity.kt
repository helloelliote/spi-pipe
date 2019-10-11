package kr.djspi.pipe01

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
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
import kotlinx.android.synthetic.main.activity_base.*
import kr.djspi.pipe01.nfc.NfcUtil
import kr.djspi.pipe01.sql.SuperviseDatabase
import kr.djspi.pipe01.util.screenScale
import kr.djspi.pipe01.util.settingsMenuEnabled
import kr.djspi.pipe01.util.toast

open class BaseActivity : AppCompatActivity(), OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    lateinit var nfcUtil: NfcUtil

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
        nmap_find.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                when {
                    currentLocation != null -> {
                        startActivity(
                            Intent(context, NaverMapActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        )
                    }
                    else -> toast(getString(R.string.toast_error_location))
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
            version.text = getString(
                R.string.nav_version_name,
                BuildConfig.VERSION_NAME,
                BuildConfig.BUILD_TYPE
            )
            val email = headerView.findViewById<TextView>(R.id.email)
            email.text = getString(R.string.nav_email, "djgis@chol.com")
            email.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_SENDTO,
                        Uri.fromParts("mailto", "djgis@chol.com", null)
                    )
                )
            }
            val phone = headerView.findViewById<TextView>(R.id.phone)
            phone.text = getString(R.string.nav_dj_phone)
            phone.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse("tel:${phone.text}")
                    )
                )
            }
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

    companion object {
        lateinit var defPackage: String
        var currentLocation: Location? = null
        var superviseDb: SuperviseDatabase? = null
        var screenRatio: Float = 0.0f
    }
}
